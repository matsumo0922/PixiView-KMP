## 1. ビルド種別の配線

- [x] 1.1 `core/common/src/commonMain/kotlin/me/matsumo/fanbox/core/common/PixiViewConfig.kt` に `isDebug: Boolean` を追加し、`dummy()` には `false` を設定する
- [x] 1.2 `shared/src/androidMain/kotlin/me/matsumo/fanbox/di/AppModule.android.kt` で `androidContext()` から `ApplicationInfo.FLAG_DEBUGGABLE` を読んで渡す。`BuildConfig` は `shared` に生成されないため使わない
- [x] 1.3 `shared/src/iosMain/kotlin/me/matsumo/fanbox/di/AppModule.ios.kt` で `Platform.isDebugBinary` を渡す。`@OptIn(ExperimentalNativeApi::class)` が必要

## 2. repository への接続

- [x] 2.1 `FanboxRepositoryImpl` のコンストラクタに `pixiViewConfig: PixiViewConfig` を追加し、`RepositoryModule.kt` で注入する
- [x] 2.2 `Fanbox` 生成時の `logLevel` を `if (pixiViewConfig.isDebug) FanboxLogLevel.INFO else FanboxLogLevel.NONE` に変更し、リリースで応答本文の断片を出さない意図を KDoc に記述する
- [x] 2.3 `FanboxRepositoryImpl` に private メソッド `recordSchemaMismatch(mismatch: FanboxListItemSchemaMismatch)` を追加する。`recordException` へ endpoint と indexPath を流す。Napier への出力は fankt が同じ内容を出すため行わない。送信内容に FANBOX / 利用者のデータが含まれない理由と Napier を省く理由を KDoc に記述する
- [x] 2.4 `getSupportedPlans()` を `fanbox.getSupportedPlans(::recordSchemaMismatch)` に変更する
- [x] 2.5 `getHomePosts()` / `getSupportedPosts()` / `getCreatorPosts()` / `getPostComment()` を callback 付き overload に変更する
- [x] 2.6 `getFollowingCreators()` / `getFollowingPixivCreators()` / `getRecommendedCreators()` / `getCreatorPlans()` を callback 付き overload に変更する
- [x] 2.7 `getBells()` を `page` / `onItemSchemaMismatch` / `markNotificationsRead = true` の 3 引数 overload に名前付き引数で変更する
- [x] 2.8 `FanboxRepository.kt` 内の `fanbox.` 呼び出しを grep し、callback overload を持つ 10 メソッドすべてが移行済みであることを確認する

## 3. 検証と仕上げ

- [x] 3.1 `./gradlew detekt` を通す
- [x] 3.2 `./gradlew test` を通す
- [x] 3.3 Android ビルドが通ることを確認する
- [x] 3.4 iOS ビルドが通ることを確認する
- [x] 3.5 `PixiViewConfig` の名前で README と AGENTS.md を grep し、誤りになった記述が無いか確認する
- [x] 3.6 `openspec validate --all --strict` を通す
