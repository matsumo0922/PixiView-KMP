## Context

`createFanbox` は `expect` / `actual` で分かれており、Android の actual だけが配信先と公開鍵を `Fanbox` へ渡す。配信先と鍵を Android の source set に置くことで iOS のバイナリへ含めない構成であり、これは `fanbox-guest-route` の既存 Requirement が定めている。

呼び出しは `FanboxRepositoryImpl` の 1 箇所だけで、`SettingDataStore` を `userDataStore` として既に保持している。生成はコンストラクタの property 初期化で同期的に起きる。

```kotlin
private val fanbox = createFanbox(
    logLevel = if (pixiViewConfig.isDebug) FanboxLogLevel.INFO else FanboxLogLevel.NONE,
    ioDispatcher = ioDispatcher,
    cookieStorage = cookieStorage,
)
```

`FanboxRepositoryImpl` は Koin の `single` であり `createdAtStart` を持たないため、この生成は最初の注入時に一度だけ起きる。

`Setting` の所在は次のとおりで、同期的に読める値が保存済みの値とは限らない。

```kotlin
val setting = settingPreference.data.map { ... }.stateIn(
    scope = CoroutineScope(ioDispatcher),
    started = SharingStarted.WhileSubscribed(1000),
    initialValue = Setting.default(),
)
```

## Goals / Non-Goals

**Goals:**

- developer mode が有効な端末だけが dev チャンネルを読み、昇格前の bundle を実機で確認できること
- developer mode が無効な場合の参照先が現在から変わらないこと
- 停止フラグ（#139）が developer mode の値によらず従来どおり機能すること

**Non-Goals:**

- 同一セッション内での切り替え（アプリの再起動なしに配信先を変える）
- ビルドの種別による分岐
- iOS での guest の起動

## Decisions

### D1. 判断の入力は `Setting.isDeveloperMode` とし、ビルドの種別では分岐しない（ユーザー確認済み）

release でも billing でも、developer mode が有効なら dev チャンネルを読む。

当初はビルドの種別（`PixiViewConfig.isDebug`）で分岐する案を採っていたが、`isDebug` は `ApplicationInfo.FLAG_DEBUGGABLE` から算出されるため、`isDebuggable = true` を持つ `billing` build type も同じ扱いになる。`billing` は release と同じ keystore で署名され `applicationIdSuffix` を持たない構成であり、「debug ビルドだけ」という前提と食い違っていた。判断の入力を設定へ移すことで、ビルドの種別と配信先の対応を考えなくてよくなる。

### D2. 保存済みの `Setting` を生成時に一度だけ読む（agent 仮決め）

`SettingDataStore.setting` は `SharingStarted.WhileSubscribed(1000)` の `StateFlow` で、初期値は `Setting.default()` である。購読が始まる前に `value` を読めば `isDeveloperMode` は常に `false` になる。新しいプロセスで `Fanbox` を生成する時点では購読が始まっている保証が無いため、この読み方では「有効にしても何も起きない」という無言の失敗になる。

`StateFlow` に対する `first()` も同じで、現在値を即座に返すだけで保存済みの値を待たない。

そこで `SettingDataStore` に保存済みの値を一度読む suspend 関数を足し、`FanboxRepositoryImpl` の生成時に `runBlocking` で読む。読むのは起動ごとに一度きりで、対象は単一の preferences ファイルである。

**この読み取りは main thread で起きる。** `FanboxRepositoryImpl` は Koin の `single` であり、最初の解決はルート Composable `PixiViewApp` の既定引数 `viewModel: PixiViewViewModel = koinViewModel()` の評価、すなわち最初のコンポジションで起きる。`PixiViewViewModel` はコンストラクタで `FanboxRepository` を受け取る。

そのため読み取りには上限を設け、超えた場合は developer mode を無効とみなす。無制限のブロックを、上限付きのブロックと fail-safe な既定へ縮退させる。

上限は 500 ミリ秒とする。単一の preferences ファイルの読み取りと復号は通常この 2 桁下のオーダーで終わるため、正常な端末でこの上限に達することは想定しない。上限は病的に遅いストレージのための打ち切りであり、その状況で失うのは開発者向けの切り替えだけで、通常の起動は続く。

値の選び方には両側に失敗がある。数秒まで許すと「有界」という文言だけが残り、体感は無制限のブロックと変わらず ANR の閾値（5 秒）へ近づく。逆に数十ミリ秒まで詰めると、コールドスタート時の初回読み取りが間に合わずに developer mode が頻繁に無効判定され、機能が実用にならない。500 ミリ秒はその中間で、通常の読み取りに対して十分な余裕があり、かつ起動の遅延として観測されても許容できる幅に収まる。

検討した代替は次のとおり。

- **`Fanbox` の生成を遅延させ、値が読めてから作る**: `fanbox` は同期的な property として多数のメソッドから参照されており、遅延させると呼び出し側まで suspend が波及する。#139 の design D2 で「同一セッション内での差し替えは採らない」と判断した論点にも当たる
- **`fanbox` を `by lazy` にして、最初の使用まで読み取りを遅らせる**: `sessionId` と `csrfToken` が property の初期化で `fanbox` に触れるため、コンストラクタの時点で強制される。遅延にならない
- **`setting` の `started` を `SharingStarted.Eagerly` へ変え、`value` を同期的に読む**: Koin は `PixiViewViewModel` のコンストラクタ引数を順に解決するため、`SettingDataStore` の生成は `FanboxRepositoryImpl` の直前に起きる。購読を即座に始めても最初のディスク読み取りは間に合わず、`value` は `Setting.default()` のままになる。ブロックを避けられる代わりに developer mode が一度も効かない
- **同期的に読める別の保存先へ値を写す**: 書き込みのたびに二重に保存する機構が増える。読み取り自体はやはりディスクを伴い、反映の時点も「次回起動」で変わらない
- **`setting.value` をそのまま読む**: 上記のとおり無言の失敗になる

### D3. 反映は次回のアプリ起動から（agent 仮決め）

`Fanbox` はプロセスごとに一度だけ生成されるため、developer mode を切り替えても同一セッション中の配信先は変わらない。#139 の停止フラグと同じ反映時点であり、利用者から見た規則が一つで済む。

### D4. 読み取りに失敗した場合と上限を超えた場合は昇格済みチャンネルへ倒す（agent 仮決め）

保存済みの値を読めなかった場合、および上限時間内に読めなかった場合は developer mode を無効とみなす。未検証の配信物を実行しない側が fail-safe であり、この向きなら読み取りの失敗が「開発者向けの機能が効かない」に留まる。

### D5. 配信先は 2 つの定数に分け、関数内で選ぶ（agent 仮決め）

`GUEST_MANIFEST_URL` を prod / dev の 2 定数に分け、引数で選ぶ。定数の形を保つことで、どちらの URL にも所在の意味を KDoc で書ける。基底 URL とチャンネル名の連結は、2 つしかない選択肢のために組み立ての規則を持ち込む。

### D6. iOS の actual は引数を受け取るが使わない（agent 仮決め）

`expect` の signature は共通であるため iOS の actual にも引数が現れる。iOS は配信先を渡さないので値は使わない。配信先を commonMain へ移す案は「配信先と鍵を Android の source set に置く」既存 Requirement に反する。

### D7. 停止フラグの分岐は変更しない（agent 仮決め）

チャンネルの選択は「停止フラグが立っていない」側の枝の中だけで起きる。フラグが立っていれば配信先を渡さない挙動は developer mode の値によらない。

### D8. どちらのチャンネルを読んだかはアプリから観測できない（agent 仮決め）

guest が起動したかどうかは `FanboxZipline` スレッドの有無で分かるが、このスレッドはロードを試みる前に作られるため、存在は「配信設定が渡された」ことしか示さない。どちらの URL を読んだかを示す出力はアプリにもライブラリにも無い。

したがって受け入れ条件 4 の確認は次の 3 つを合わせて行う。(1) 選択のロジックを unit test で固定する (2) ビルド成果物に dev チャンネルの URL が実際に含まれることを確認する (3) 実機で guest が退避せずに動作することを確認する。「その URL へ取得しに行った」ことの直接の観測は含まれない。

## Risks / Trade-offs

- **`developerPassword` を取得した第三者が、リリース版で dev チャンネルを読める**（ユーザー確認済みの受容） → `PixiViewConfig.developerPassword` はリリース APK へ焼き込まれ逆コンパイルで取得しうる。取得した者は自分の端末で developer mode を有効にし、昇格前の bundle を実行させられる。ただし公開鍵の検証は変わらず働くため、実行できるのは fankt が署名した未昇格のコードに限られ、任意のコードではない。また `isDeveloperMode` は `Setting.hasPrivilege` を通じて既に有料機能を解放しており、パスワードの漏洩による影響はこの change 以前から存在する
- **起動時の main thread に同期的な読み取りが 1 回入る**（D2） → developer mode の値によらず、すべてのビルド・すべての利用者で毎起動 1 回起きる。ブロックされるのは最初のコンポジションを行う main thread である。上限を設けて最悪時間を有界にし、超過時は安全側へ倒すが、上限までの遅延自体は残る。既存コードに production の `runBlocking` は無く、これが最初の 1 件になる（既存の 4 件はいずれもテスト）
- **dev チャンネルには昇格前の bundle が乗る** → developer mode の端末は壊れた bundle を実行しうる。これは意図した挙動である。退避の経路は変わらないため、bundle が壊れていても投稿詳細の取得自体は直接経路で成立する
- **iOS の actual に使わない引数が残る**（D6） → 静的解析が未使用引数を指摘する可能性がある。指摘された場合は expect/actual の制約であることを示す抑制で閉じる
- **実機での確認がチャンネルを直接は示さない**（D8） → 選択の証明は unit test と成果物の検査に依る

## Migration Plan

developer mode が無効な端末の参照先は変わらないため、利用者側の影響は無い。developer mode が有効な端末は次回の起動から dev チャンネルを読む。

rollback は 2 定数を 1 つに戻して分岐を外せば足りる。`SettingDataStore` へ足した読み取り関数は他から使われないため、併せて外せる。

## Open Questions

なし。
