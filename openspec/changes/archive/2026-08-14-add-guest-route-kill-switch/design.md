# 設計

## 現状の把握

| 事実 | 確認箇所 |
| --- | --- |
| `Fanbox` は `FanboxRepositoryImpl` の構築時に一度だけ生成される | `FanboxRepository.kt:239` |
| `FanboxRepositoryImpl` は `single`（`createdAtStart` なし）で、最初の注入時に構築される | `di/RepositoryModule.kt:41`。リポジトリ全体に `createdAtStart` の使用は無い |
| `FirebaseApp` は `Application.onCreate()` の手動初期化でのみ生成される | `PixiViewApplication.kt:37`, `PixiViewApplication.kt:74`。`FirebaseInitProvider` は `androidApp/src/main/AndroidManifest.xml:98` で `tools:node="remove"` により除去されている |
| Koin の設定は `KoinStartup` により androidx App Startup 経由で走り、`Application.onCreate()` より前に完了する | `PixiViewApplication.kt:26`, `PixiViewApplication.kt:41` |
| guest 用と直接経路用でコンストラクタが分かれており、生成は `androidMain` / `iosMain` に委ねられている | `FanboxFactory.android.kt` / `FanboxFactory.ios.kt` |
| `firebase-config` は依存に含まれ、`core:common` の `androidMain` が `api` で公開している | `libs.versions.toml:193`, `core/common/build.gradle.kts` |
| Kotlin から Remote Config を参照している箇所は無い | `firebase-config` は依存にあるが未使用 |
| guest bundle のローカルキャッシュは存在しない | `ZiplineGuestLoader.newLoader` が `ZiplineLoader` に `withCache` を渡していない。`FreshnessChecker` も `AlwaysStale` |

Koin の設定自体は `Application.onCreate()` より前に完了するが、`single` は遅延構築であり、`FanboxRepositoryImpl` は最初の注入時に初めて構築される。リポジトリ全体に `createdAtStart` の使用は無く、`androidApp` に Koin から `FanboxRepository` を取り出す eager な Initializer も無いため、最初の注入は UI からの取得、すなわち `Application.onCreate()` 完了後に起きる。したがって `createFanbox` の時点で `FirebaseApp` は初期化済みである。

この成立条件は将来の変更で崩れうる。`createdAtStart = true` の付与や、Koin へ依存する新しい Initializer の追加により `createFanbox` が `Application.onCreate()` より前に走ると、`FirebaseInitProvider` を除去している以上 `FirebaseApp` は存在せず、Remote Config の参照は例外になる。D5 によりアプリは落ちないが、停止フラグは常に「立っていない」と判定され、停止スイッチが無言で効かなくなる。D5 の失敗は記録に残すが、この劣化に限っては記録も届かない。記録先の Crashlytics も既定の `FirebaseApp` を要するため、`FirebaseApp` が無い状態では記録する手段そのものが無い。記録が効くのは `FirebaseApp` が初期化済みで参照だけが失敗する場合である。

## 決定

### D1: 停止フラグは `createFanbox` の中で読む（構造の判断）

停止の対象は「配信先と公開鍵を渡した `Fanbox` を作ること」そのものである。判断を生成箇所に置けば、分岐は `if` ひとつで済み、`FanboxRepositoryImpl` にも `commonMain` にも新しい引数・インターフェース・注入経路が要らない。フラグを `commonMain` へ持ち上げると iOS 側に意味のない `actual` が生まれる。

### D2: 反映は次回のアプリ起動時とする（ユーザー確認済み）

`createFanbox` は Remote Config の**活性化済みの値を同期的に読み**、同じ関数で取得を背景に走らせる。取得した値は次回の起動で読まれる。

同一セッション内での差し替えを採らない理由は次のとおり。`fanbox` は `FanboxRepositoryImpl` の `val` であり、差し替えるには可変化、進行中の呼び出しとの競合の扱い、古いインスタンスの `close` の時機、`cookieStorage` の共有の 4 点を新たに設計することになる。停止スイッチが対応する 3 つの事象（proposal 参照）はいずれも「アプリを開き直せば直る」で実害が収まる。取得が返らない事象では利用者はどのみち再起動する。

### D3: フラグは「停止」を表す boolean とし、既定値の設定を持たない（ユーザー確認済み）

キー名を `android_guest_route_kill_switch` とし、値 `true` を「guest を起動しない」の意味に割り当てる。

Remote Config は、活性化済みの値も `setDefaults` の値も無い boolean に対して静的既定の `false` を返す（Firebase Android SDK の `FirebaseRemoteConfig.getBoolean` の規定。BOM `34.14.1` の `firebase-config` を使用）。キーを「停止」の向きで定義すると、この静的既定がそのまま「停止していない = guest を起動する」になる。`setDefaultsAsync` を呼ぶ必要が無くなり、その完了を待たずに `getBoolean` を読む競合も生じない。

この静的既定は SDK の挙動であり、本リポジトリからは検証できない。誤っていれば「フラグを一度も取得していない端末で guest が起動しない」という形で現れる。新規インストール直後の初回起動で guest が起動することを実機で確認し、前提の成立を確かめる。

**取得前および取得失敗時は guest を起動する側へ倒す**。これが本 change で唯一のリスク許容の判断であり、ユーザーが確定した。

- 倒さない側（既定で停止）を採ると、Remote Config を一度も取得できていない端末——新規インストールの初回起動、および Firebase へ到達できない端末——で guest が起動しなくなる。`fanbox-guest-route` が成立する条件が「Firebase の取得に成功していること」になり、#138 で有効化した配信が Firebase の障害で全端末から失われる
- 倒す側の残余リスクは、停止を決めてから各端末の次回起動まで壊れた bundle が実行され続けることである。ただし配信元の巻き戻しが同時に使え、そちらは端末側の状態に依存せず即座に効く。停止スイッチは配信元が使えない場合の手段であり、第一の手段ではない

採らなかった選択肢と分岐条件：guest の誤動作がデータの破壊や外部への送信を伴い得るなら、既定で停止する側（`android_guest_route_enabled` を `setDefaultsAsync` で `false`、取得成功後にのみ有効化）へ倒す。現在の guest の職掌は `post.info` の要求組み立てと応答解析に限られ、書き込みも送信も行わないため、この条件には当たらないと判断した。

### D4: 取得間隔は 1 時間とする（ユーザー確認済み）

`minimumFetchIntervalInSeconds` の Firebase 既定は 12 時間である。停止スイッチの反映までの遅延は「取得間隔 + 次回起動まで」であり、12 時間は障害対応の道具として長い。1 時間へ短縮する。

設定は永続化されるため、設定の書き込みと取得の順序が最初の 1 回だけ問題になる。`setConfigSettingsAsync` の完了後に取得を開始することで、この 1 回も既定の 12 時間で走らないようにする。

### D5: Remote Config の参照が例外を投げてもアプリを落とさない（構造の判断）

guest 経路の設計は「どの失敗も直接経路へ退避する」で一貫している（`FanboxGuestHost.guestOrNull` は `Throwable` を捕捉する）。停止スイッチ自身がアプリを落とすと、この一貫性が反転し、Firebase の不調がアプリの起動不能になる。参照全体を `runCatching` で包み、失敗時は D3 の既定（guest を起動する）へ倒す。

退避と同時に失敗を記録する。`FanboxGuestHost` は退避のたびに `diagnosticSink` へ報告しており、記録の無い退避はこの経路だけが例外になる。停止スイッチの失敗は「停止フラグを倒しても止まらない」という無言の症状しか出さないため、記録が無いと原因に到達できない。記録には `core:common` の `recordException` を使う。`FanboxRepositoryImpl` が既に使っており、新しい仕組みを要さない。

## 実装の形

```kotlin
private const val GUEST_ROUTE_KILL_SWITCH_KEY = "android_guest_route_kill_switch"
private const val REMOTE_CONFIG_FETCH_INTERVAL_SECONDS = 3600L

internal actual fun createFanbox(
    logLevel: FanboxLogLevel,
    ioDispatcher: CoroutineDispatcher,
    cookieStorage: FanboxCookieStorage,
): Fanbox {
    if (isGuestRouteKilled()) {
        return Fanbox(
            logLevel = logLevel,
            ioDispatcher = ioDispatcher,
            cookieStorage = cookieStorage,
        )
    }

    return Fanbox(
        guestManifestUrl = GUEST_MANIFEST_URL,
        guestTrustedKeyName = GUEST_TRUSTED_KEY_NAME,
        guestTrustedEd25519PublicKey = GUEST_TRUSTED_ED25519_PUBLIC_KEY_HEX.hexToByteArray(),
        logLevel = logLevel,
        ioDispatcher = ioDispatcher,
        cookieStorage = cookieStorage,
    )
}

private fun isGuestRouteKilled(): Boolean = runCatching {
    val remoteConfig = Firebase.remoteConfig

    remoteConfig.setConfigSettingsAsync(
        remoteConfigSettings {
            minimumFetchIntervalInSeconds = REMOTE_CONFIG_FETCH_INTERVAL_SECONDS
        },
    ).addOnCompleteListener { remoteConfig.fetchAndActivate() }

    remoteConfig.getBoolean(GUEST_ROUTE_KILL_SWITCH_KEY)
}.getOrElse { failure ->
    runCatching { recordException(failure) }
    false
}
```

記録そのものも守る。`recordException` の Android 実装は `Firebase.crashlytics` を解決するため、`Firebase.remoteConfig` と同じ理由——既定の `FirebaseApp` が無い——で失敗する。守らずに置くと、D5 が最も要る場面、すなわち `FirebaseApp` が未初期化のまま `createFanbox` が走る場面でだけ、退避のための記録がそのままクラッシュになる。

`getBoolean` は活性化済みの値を返し、取得の完了を待たない。取得は次回の起動へ効く。

## 残余リスク

- **`FirebaseApp` が無い状態は観測できない**。`createFanbox` が `Application.onCreate()` より前に走るようになった場合、停止スイッチは無言で効かなくなり、記録先も同時に失われるため痕跡が残らない。現状この経路は存在しないが、成立条件はコードで強制されておらず、`createdAtStart` の付与や Koin へ依存する Initializer の追加で崩れる
- **停止の反映に遅延がある**。停止を決めてから、各端末で「取得間隔 1 時間」の経過と次回起動の両方が起きるまで、guest は実行され続ける。即座に全端末へ効かせる手段は本 change に含まれない
- **Firebase コンソール側の設定は本リポジトリの外にある**。キー名 `android_guest_route_kill_switch` の綴りがコンソールの設定と食い違っても、症状は「停止スイッチを倒しても止まらない」という無言の失敗になる。倒した後に実機で停止を確認する手順を受け入れ条件に置く
- **`getBoolean` が初回に同期のディスク読み出しを起こしうる**。`createFanbox` は最初の注入時に呼ばれ、呼び出しスレッドは Koin の注入元に従う。アプリは起動時に DataStore の読み出しを既に行っており、新たな種類の負荷ではない
- **debug ビルドと release ビルドが同じ Firebase プロジェクトの別 application id を使う**。両方を同時に止めたい場合は、コンソールの条件をアプリ単位ではなくプロジェクト既定として設定する必要がある

## 検証

Remote Config は端末上の Firebase サービスに依存し、`androidHostTest`（JVM）からは動かせない。本 change の分岐は `if (停止フラグ) 直接経路 else guest` の 1 段であり、単体テストで固定できるのはこの自明な対応だけになるため、テストは追加しない。

検証は次の 2 つで行う。

1. リリースビルドが通ること（`./gradlew :androidApp:assembleRelease` 相当）
2. 実機で、Firebase コンソールのフラグを `true` へ倒した後にアプリを再起動すると guest が起動しないこと、`false` へ戻して再起動すると guest が起動すること。guest の起動有無は debug ビルドの `FanboxDiagnosticSink` の出力で判別する
