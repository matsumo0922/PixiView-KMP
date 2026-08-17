担当 PR は design.md D10 の Stack 構成に対応する。

| 段 | branch |
| --- | --- |
| 2 | `feature/app-owned-fanbox-models-defs` |
| 3 | `feature/app-owned-fanbox-models-swap` |

## 1. モデル定義（段 2 / `-defs`）

- [ ] 1.1 `core/model/src/commonMain/.../core/model/fanbox/` に ID の value class 7 種（`PostId` / `PostItemId` / `CreatorId` / `UserId` / `CommentId` / `PlanId` / `NewsLetterId`）を定義する
- [ ] 1.2 葉のモデル（`Cover` / `Tag` / `User` / `Creator` / `PaymentMethod`）を定義する
- [ ] 1.3 集約のモデル（`Post` / `CreatorDetail` / `CreatorPlan` / `CreatorPlanDetail` / `Comment` / `Bell` / `MetaData` / `NewsLetter` / `PaidRecord`）を定義する
- [ ] 1.4 `PostDetail` と `Body` の sealed 階層、`OtherPost` / `ImageItem` / `VideoItem` / `FileItem` を定義し、D7 の表で「する」とした派生メンバー 11 個を移植する
- [ ] 1.5 ページング型（`PageCursorInfo` / `PageNumberInfo` / `PageOffsetInfo` / `Cursor`）を定義する
- [ ] 1.6 Composable から参照するモデルへ `@Immutable` を付ける（AGENTS.md の Compose 規約）
- [ ] 1.7 各 `data class` / `enum` / `value class` に日本語 KDoc を付ける

## 2. 変換層（段 2 / `-defs`）

- [ ] 2.1 `core/repository/src/commonMain/.../repository/mapper/` に fankt → app の変換を全型分置く
- [ ] 2.2 app → fankt の変換を D5 の 4 種（ID / `Post` / `Cursor`、およびページング型）に限って置く
- [ ] 2.3 `PostDetail.Body` と記事ブロックの変換を、`when` を exhaustive に書いて全 variant 分置く

## 3. 変換のテスト（段 2 / `-defs`）

- [ ] 3.1 全フィールドを埋めた `FanboxPost` / `FanboxPostDetail` / `FanboxCreatorDetail` の変換で、各フィールドが写ることを固定する
- [ ] 3.2 `Body` の 7 variant と記事ブロックの 6 variant がすべて変換されることを固定する
- [ ] 3.3 未知の variant が生の JSON を保持することを固定する
- [ ] 3.4 ID / `Post` / `Cursor` の往復が元の値へ戻ることを固定する
- [ ] 3.5 D7 の派生メンバー 11 個が fankt と同じ値を返すことを固定する

## 4. 公開契約の置き換え（段 3 / `-swap`）

- [ ] 4.1 `FanboxRepository` の interface とその実装をアプリ所有のモデルへ差し替え、`BookmarkDataStore` / `BlockDataStore` との境界で変換する
- [ ] 4.2 `core/repository/paging/` の 5 本の `PagingSource` を差し替える
- [ ] 4.3 `TranslationRepository` の `translate` 2 つと、`DownloadPostsRepository` の interface および android / ios の実装を差し替える（D5）
- [ ] 4.4 `core/model` の既存ファイル（`Destination` / `FanboxDownloadItems` / `PostDownloader` / `TransPostDetail` / `TransComments`）を差し替え、`RequestType.Post` を `WholePost` へ改名する（D8）
- [ ] 4.5 `core/ui` の 6 ファイルを差し替える（`NavTypes` の符号化が変わらないことを含む）
- [ ] 4.6 `feature/post` 27 ファイルを差し替える
- [ ] 4.7 `feature/creator` 21 ファイルを差し替える
- [ ] 4.8 `feature/library` 14 ファイルを差し替える
- [ ] 4.9 `feature/setting` 3 / `feature/welcome` 1 / `shared` 1 を差し替える

## 5. 永続化の非退行（段 3 / `-swap`）

- [ ] 5.1 `core/datastore` が変更されていないことを確認する
- [ ] 5.2 置き換え前の形式で保存されたブックマーク JSON が読めることを固定する
- [ ] 5.3 保存される JSON が置き換え前と同じ形式であることを固定する
- [ ] 5.4 ブロック済みクリエイターが読めることを固定する
- [ ] 5.5 `PostId` / `CreatorId` の画面遷移引数の符号化が変わらないことを固定する

## 6. 検証とドキュメント（段 3 / `-swap`）

- [ ] 6.1 `feature/*` / `core/ui` / `core/model` / `shared` に `me.matsumo.fankt.fanbox.domain` の import が残っていないことを走査で確認する
- [ ] 6.2 `./gradlew detekt` と既存テストを通す
- [ ] 6.3 Android と iOS の両方でコンパイルが通ることを確認する
- [ ] 6.4 `README.md` の Architecture 節に `core/model` と `core/repository` の責務を反映する
- [ ] 6.5 変更した機能名・クラス名で `docs/` と `README.md` を走査し、誤りになった記述がないか確認する
