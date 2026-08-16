## Context

`createFanbox` は `expect` / `actual` で分かれており、Android の actual だけが配信先と公開鍵を `Fanbox` へ渡す。配信先と鍵を Android の source set に置くことで iOS のバイナリへ含めない構成であり、これは `fanbox-guest-route` の既存 Requirement が定めている。

呼び出しは `FanboxRepositoryImpl` の 1 箇所だけで、`PixiViewConfig` を既に保持している。現在は `isDebug` から導出した `logLevel` を渡しており、`isDebug` そのものは渡していない。

```kotlin
private val fanbox = createFanbox(
    logLevel = if (pixiViewConfig.isDebug) FanboxLogLevel.INFO else FanboxLogLevel.NONE,
    ioDispatcher = ioDispatcher,
    cookieStorage = cookieStorage,
)
```

`FanboxRepositoryImpl` は Koin の `single` であり `createdAtStart` を持たないため、この生成は最初の注入時に起きる。

## Goals / Non-Goals

**Goals:**

- debug ビルドが dev チャンネルを読み、昇格前の bundle を実機で確認できること
- リリースビルドの参照先が現在から変わらないこと
- 停止フラグ（#139）が両方のビルドで従来どおり機能すること

**Non-Goals:**

- 実行時のチャンネル切り替え（`isDeveloperMode` などによる）
- ストア版から dev チャンネルを読む手段
- iOS での guest の起動

## Decisions

### D1. `createFanbox` に `isDebug: Boolean` を足す（agent 仮決め）

判断に要るのは `isDebug` そのものであり、呼び出し側が既に持っている。検討した代替は次のとおり。

- **`PixiViewConfig` を渡す**: 必要なのは 1 つの Boolean で、config 全体への依存は広すぎる
- **androidMain で `ApplicationInfo.FLAG_DEBUGGABLE` から導出する**: `Context` が要り、`core/repository` はそれを持たない。同じ導出は `AppModule.android.kt` で既に行われ `PixiViewConfig.isDebug` になっている。同じ判定を 2 箇所で行わない
- **`logLevel` から導出する**（`NONE` 以外なら dev）: 診断の詳細度と配信先の選択という別の関心を 1 つの値へ束ねることになる。片方だけ変えたい場合に破綻する

### D2. 配信先は 2 つの定数に分け、関数内で選ぶ（agent 仮決め）

`GUEST_MANIFEST_URL` を「prod」「dev」の 2 定数に分け、`isDebug` で選ぶ。定数の形を保つことで、どちらの URL にも所在の意味を KDoc で書ける。URL を組み立てる形（基底 URL + チャンネル名の連結）は取らない。連結は 2 つしかない選択肢のために組み立ての規則を持ち込む。

### D3. `Setting.isDeveloperMode` では切り替えない（ユーザー確認済み）

`createFanbox` は `FanboxRepositoryImpl` のコンストラクタで同期的に呼ばれる。`isDeveloperMode` は `SettingDataStore` の Flow であり同期的に読めない。読むには `runBlocking` を挟むか `Fanbox` の生成を遅延させる構造変更が要り、後者は #139 の design D2 で「同一セッション内での差し替えは採らない」と判断した論点と同じ壁に当たる。

加えて `isDeveloperMode` は `Setting.hasPrivilege` と `canBulkDownload` を通じて有料機能を解放する。`PixiViewConfig.developerPassword` はリリース APK へ焼き込まれ逆コンパイルで取得しうるため、そこへ遠隔コードの取得先の選択を足すと漏洩時の影響範囲が広がる。

### D4. iOS の actual は引数を受け取るが使わない（agent 仮決め）

`expect` の signature は共通であるため、iOS の actual にも `isDebug` が現れる。iOS は配信先を渡さないので値は使わない。iOS 側だけ signature を変える手段は無く、配信先を commonMain へ移す案は「配信先と鍵を Android の source set に置く」既存 Requirement に反する。

### D5. 停止フラグの分岐は変更しない（agent 仮決め）

チャンネルの選択は「停止フラグが立っていない」側の枝の中だけで起きる。フラグが立っていれば配信先を渡さない挙動は両ビルドで変わらない。

### D6. どちらのチャンネルを読んだかはアプリから観測できない（agent 仮決め）

guest が起動したかどうかは `FanboxZipline` スレッドの有無で分かるが、このスレッドはロードを試みる前に作られるため、存在は「配信設定が渡された」ことしか示さない。どちらの URL を読んだかを示す出力はアプリにもライブラリにも無い。

したがって選択の正しさは unit test で示し、実機では「debug ビルドで guest が退避せずに動作する」ことの確認に留める。両者を合わせても「dev チャンネルを読んだ」ことの直接の観測にはならない。この限界は受け入れ条件 4 の解釈として記録する。

## Risks / Trade-offs

- **dev チャンネルには昇格前の bundle が乗る** → debug ビルドは壊れた bundle を実行しうる。これは意図した挙動であり、そのために dev チャンネルがある。退避の経路は変わらないため、bundle が壊れていても投稿詳細の取得自体は直接経路で成立する
- **iOS の actual に使わない引数が残る** → 静的解析が未使用引数を指摘する可能性がある。指摘された場合は expect/actual の制約であることを示す抑制で閉じる
- **実機での確認がチャンネルを直接は示さない**（D6） → 選択の証明は unit test に依る

## Migration Plan

リリースビルドの参照先は変わらないため、利用者側の影響は無い。debug ビルドは次回のビルドから dev チャンネルを読む。

rollback は 2 定数を 1 つに戻して分岐を外せば足りる。

## Open Questions

なし。
