前提: 先行 change `adopt-tolerant-list-callbacks` が完了していること。両者は `FanboxRepository.kt` の同じメソッド本体を書き換える。

## 1. 分類器の実装

- [ ] 1.1 `core/model/src/commonMain/kotlin/me/matsumo/fanbox/core/model/FanboxErrorKind.kt` に `enum class FanboxErrorKind` を定義し、各分類がどの `FanboxException` subtype に対応するかを KDoc に記述する
- [ ] 1.2 同ファイルに `internal fun fanboxErrorKindOf(statusCode: Int?): FanboxErrorKind?` を実装する。401/403/404/429/5xx を分類し、それ以外と null は `null` を返す
- [ ] 1.3 同ファイルに `Throwable.toFanboxErrorKind()` を実装する。`SchemaMismatch` と `Network` のみ型で分岐し、他の `FanboxException` は `fanboxErrorKindOf(statusCode)` に委ねる。中断は呼び出し側で除外済みである前提を KDoc に記述する

## 2. 文言とファクトリ

- [ ] 2.1 `core/resources/src/commonMain/composeResources/values/strings.xml` に `error_session_expired` / `error_forbidden` / `error_rate_limited` / `error_server` / `error_schema_mismatch` を追加する。他ロケールへは追加せず英語フォールバックとする
- [ ] 2.2 `Throwable.toScreenStateError(fallbackMessage, fallbackRetryTitle)` を `core:model` に実装する。design.md の対応表に従い、`Unknown` のときのみ fallback の 2 つを使う

## 3. ViewModel の置き換え

- [x] 3.1 `feature/creator` の 6 箇所（`CreatorTopViewModel` / `PaymentsViewModel` / `FollowingCreatorsViewModel` / `FanCardViewModel` / `SupportingCreatorsViewModel` / `CreatorPostsDownloadViewModel`）を `it.toScreenStateError(...)` に置き換える。`FanCardViewModel` は `fallbackMessage = creator_fan_card_not_supported` と `fallbackRetryTitle = common_back` を渡し、既存の表示を保つ
- [x] 3.2 `feature/post` の 4 箇所（`BookmarkedPostsViewModel` / `PostImageViewModel` / `PostByCreatorSearchViewModel` / `PostDetailViewModel`）を置き換える
- [x] 3.3 `feature/library` の 2 箇所（`LibraryDiscoveryViewModel` / `LibraryMessageViewModel`）を置き換える
- [x] 3.4 `BillingViewModel` / `PostDetailScreen:237` / `LazyPagingItemsLoadContents:59` の 3 箇所は FANBOX API の失敗を直接受けないため置き換えないことを確認する

## 4. セッション無効化の検知

- [x] 4.1 `FanboxRepository` interface に `sessionInvalidatedTrigger: Flow<Long>` を追加する
- [x] 4.2 `FanboxRepositoryImpl` に `Channel<Long>(Channel.CONFLATED)` の実体を追加する
- [x] 4.3 `FanboxRepositoryImpl` に `private suspend fun <T> withSessionCheck(block: suspend () -> T): T` を実装する。`FanboxException.Unauthorized` を捕捉して通知し、例外は再スローする
- [x] 4.4 fankt の HTTP を伴うメソッドを `withSessionCheck` で包む。`logout()` とセッション設定系は除外し、除外理由を KDoc に記述する。先行 change で追加した `::recordSchemaMismatch` の呼び出しを壊さないこと
- [x] 4.5 `FanboxRepository.kt` 内の `fanbox.` 呼び出しを grep し、除外リスト（`logout()` とセッション設定系）以外がすべて `withSessionCheck` で包まれていることを確認する
- [x] 4.6 `PixiViewViewModel` の `init` に `sessionInvalidatedTrigger` の購読を追加し、`_isLoggedInFlow.emit(false)` する

## 5. テスト

- [x] 5.1 `fanboxErrorKindOf()` が 401/403/404/429/500/503 を正しい分類へ変換し、400 と null で `null` を返すことを検証するテストを `core/model` の `commonTest` に追加する
- [x] 5.2 `FanboxException` 以外の `Throwable` が `toFanboxErrorKind()` で `Unknown` になることを検証する
- [x] 5.3 `toScreenStateError()` が `Unknown` のとき `fallbackMessage` と `fallbackRetryTitle` を保持することを検証する

## 6. 検証と仕上げ

- [x] 6.1 `./gradlew detekt` を通す
- [x] 6.2 `./gradlew test` を通す
- [x] 6.3 Android ビルドが通ることを確認する
- [x] 6.4 iOS ビルドが通ることを確認する
- [ ] 6.5 セッションを無効化した状態で任意の画面を開き、welcome フローへ差し替わることを確認する
- [x] 6.6 `ScreenState` / `FanboxErrorKind` / `FanboxRepository` の名前で README と AGENTS.md を grep し、誤りになった記述が無いか確認する
- [x] 6.7 `openspec validate --all --strict` を通す
