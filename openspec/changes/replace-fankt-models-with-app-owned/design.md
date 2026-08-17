## Context

`FanboxRepository` は fankt の domain model をそのまま返し、`feature/*` と `core/ui` はそれを直接描画している。`me.matsumo.fankt` を参照するファイルは 102、import 行は約 260（うち `domain.model.*` が 219）。fankt のモデルが変わると UI 層まで一斉に壊れる。

fankt 側の domain model は 24 ファイル / 749 行で、大半は素朴な `data class` である。例外は `FanboxPostDetail`（272 行）で、記事本文を表す `Body` の sealed 階層と派生プロパティ（`browserUrl` / `Body.imageItems` / `Block.Embed.url` / `FileItem.asImageItem()`）を持つ。

`core/datastore` の 11 ファイルも fankt を参照するが、内訳は cookie / session の基盤契約（`FanboxCookieStorage` / `FanboxCookieRecord`）が大半で、domain model を使うのは `BookmarkDataStore`（`FanboxPost` を JSON で丸ごと保存）と `BlockDataStore`（`FanboxCreatorId`）の 2 本である。

## Goals / Non-Goals

**Goals:**

- `FanboxRepository` の public API、`feature/*`、`core/ui`、`core/model`、`shared` から `me.matsumo.fankt` の型を外す
- 既存ユーザーのブックマークとブロック設定が、更新後も同じように読める
- fankt のモデルが変わったとき、修正が `core/repository` の変換層で止まる

**Non-Goals:**

- `core/datastore` から fankt を外すこと。cookie / session 系は fankt が要求する基盤契約であり、domain model ではない
- `FanboxException` / `FanboxErrorKind` / `FanboxListItemSchemaMismatch` の置き換え。これらは例外と診断の契約で、`fanbox-error-classification` と `fanbox-schema-diagnostics` が既に規定している
- fankt のバージョン更新
- app-owned モデルへの独自フィールド追加。今回は fankt のモデルと 1:1 の写像に限る

## Decisions

### D1 命名は `me.matsumo.fanbox.core.model.fanbox.*` で `Fanbox` prefix を持たない（ユーザー確認済み）

`Post` / `PostDetail` / `CreatorDetail` / `PostId` のように書く。fankt 型と同名にすると import の取り違えがコンパイルを通ってしまうため、名前で区別できる状態を選ぶ。`core/model` の既存型（`Setting` / `Flag` / `Version`）とも揃う。

採らなかった案：`FanboxPost` のまま package だけ変える。`core/model` の既存 `FanboxDownloadItems` / `FanboxErrorKind` とは揃うが、同名の型が 2 つ存在する状態を全 102 ファイルへ持ち込む。

### D2 ID は value class を維持する（ユーザー確認済み）

`@JvmInline value class PostId(val value: String)` として fankt と 1:1 に対応させる。`PostId` と `CreatorId` の取り違えをコンパイラが検出できる状態を保つ。`value` が `String` である点も fankt と同じなので、`NavTypes.kt` の URL エンコード（`encode = { it.value }`）と `BlockDataStore` の `stringSetPreferencesKey` はどちらも表現が変わらない。

採らなかった案：素の `String`。変換層は消えるが、参照数の多い `PostId`（142）と `CreatorId`（140）で型の取り違えが起きうる。

### D3 永続化の形式は変えない（ユーザー確認済み）

`BookmarkDataStore` は `Json.encodeToString(FanboxPost.serializer(), post)` で fankt の `FanboxPost` を丸ごと保存している。この serializer を使い続け、`core/datastore` は変更しない。`FanboxRepositoryImpl` が app-owned の `Post` と fankt の `FanboxPost` を相互変換して datastore へ渡す。

保存済み JSON に触らないため migration は発生しない。#117（bookmark の JSON 非互換）と同種のリスクを持ち込まない。

採らなかった案：app-owned モデルで保存し直す。fankt から完全に切れるが、既存ユーザーのブックマークに対する migration が必要になる。

**この決定の帰結（residual risk）**：app-owned の `Post` に fankt へ存在しないフィールドを足すと、その値は保存されない。今回の Non-Goals で 1:1 写像に限っているため現時点では顕在化しないが、将来 `Post` を拡張する際はこの制約に当たる。

### D4 変換層は `core/repository` 内の `mapper` package へ置く（agent 仮決め）

`me.matsumo.fanbox.core.repository.mapper` に fankt → app の拡張関数を置く。専用 module を作らない。`core/repository` は既に fankt と `core/model` の両方に依存しており、新しい依存も新しいビルド設定も要らない。

issue #137 の未決事項「変換層の置き場所（`core/repository` 内か、専用 module か）」への回答である。

### D5 変換は fankt → app を全型に用意し、app → fankt は必要な 4 種に限る（agent 仮決め）

逆方向が要るのは次の経路だけである。

| 逆変換が要る型 | 理由 |
| --- | --- |
| `PostId` / `CreatorId` / `UserId` / `CommentId` | `FanboxRepository` の引数を fankt の API へ渡す |
| `Post` | `BookmarkDataStore.save` / `remove` が fankt の serializer を使う（D3） |
| `Cursor` | paging で fankt へ次ページ位置を渡す |

`PostDetail` / `CreatorDetail` / `Comment` / `Bell` などは表示専用で、アプリから fankt へ戻す経路がない。使われない逆変換を書かない。

### D6 ページング型もアプリ所有にする（agent 仮決め）

`PageCursorInfo<T>` / `PageNumberInfo<T>` / `PageOffsetInfo<T>` / `FanboxCursor` は `me.matsumo.fankt.fanbox.domain` にあり、`FanboxRepository` の戻り値に現れる。受け入れ条件が `me.matsumo.fankt` の型全般を対象にしているため、これらも `core/model/fanbox` へ写す（`PageCursorInfo` / `PageNumberInfo` / `PageOffsetInfo` / `Cursor`）。

### D7 派生プロパティは app-owned モデルへ移植する（agent 仮決め）

`FanboxPostDetail.browserUrl`、`Body.imageItems` / `fileItems`、`Block.Embed.url`、`Body.Video.url`、`FileItem.asImageItem()` / `asVideoItem()` は fankt 側で計算されている。app-owned モデルへ同じ実装を移す。

これは意図した重複である。分離の目的は「fankt が変わってもアプリが壊れない」ことであり、これらの派生ロジックが fankt 側で変わってもアプリは追随しなくてよい。逆に、FANBOX の URL 形式が変わった場合はアプリ側も直す必要がある。

### D8 `FanboxDownloadItems.RequestType.Post` を `WholePost` へ改名する（agent 仮決め）

`RequestType.Post` は `val post: FanboxPost?` を持つ。app-owned 型を `Post` にすると、この入れ子クラスの本体では `Post` が自分自身へ解決され、フィールドの型を書けない。

`RequestType.WholePost` へ改名する。参照は 5 ファイル 12 箇所で、いずれも `FanboxDownloadItems.RequestType.Post` の形で完全に修飾されている。

採らなかった案：この 1 ファイルだけ import alias を使う。型の別名がファイルごとに異なる状態を作るため採らない。

`Destination.PostDetail` と `PostsLog.Comment` も新しいモデル名と重なるが、どちらも所有クラス経由（`Destination.PostDetail`）で参照されており衝突しない。

### D9 `kotlin.time.Instant` はそのまま使う（agent 仮決め）

fankt の日時は `kotlin.time.Instant`（stdlib）である。アプリ独自の日時型は作らない。fankt が別の日時型へ移った場合は変換層で吸収する。

### D10 配送は 4 段の Stack とする（agent 仮決め）

`FanboxRepository` の戻り値型を変えた瞬間に全呼び出し元がコンパイルを失うため、変換層の導入と呼び出し元の置換は 1 つのコミット単位から分けられない。分離できるのはモデル定義だけである。

| 段 | branch | 内容 |
| --- | --- | --- |
| 1 | `feature/app-owned-fanbox-models` | proposal / delta spec / design / tasks |
| 2 | `feature/app-owned-fanbox-models-defs` | `core/model/fanbox/*` のモデル定義と変換層、変換のテスト（純追加。既存コードに触れない） |
| 3 | `feature/app-owned-fanbox-models-swap` | `FanboxRepository` の public API と全呼び出し元の置換（atomic） |
| 4 | `feature/app-owned-fanbox-models-archive` | main spec への同期と archive |

2 段目は 3 段目が無ければ誰にも使われないが、「fankt のモデルと形が一致しているか」「変換が値を落としていないか」を 3 段目の機械的な置換 90 ファイルと切り離してレビューできる。この分離が Stack を選ぶ理由である。

## Risks / Trade-offs

- **変換でフィールドを取り違える** → 変換のテストで全フィールドを固定する。取り違えは「値が入れ替わって表示される」形で出るため、コンパイルでは捕まらない
- **`Body` の sealed 階層の variant を取りこぼす** → `when` を exhaustive に書き、全 variant を通す変換テストを置く。`Unknown` variant（`rawJson` / `rawBodyJson`）の保持も含める
- **`BookmarkDataStore` の JSON 保存形式が意図せず変わる** → `core/datastore` を変更しないことで構造的に防ぐ。保存済み JSON を読んで `Post` として返せることをテストで固定する
- **90 ファイルの機械的置換で挙動が変わる** → 置換は import と型名の差し替えに限り、ロジックへ触れない。既存テストの通過と両プラットフォームのコンパイルで担保する
- **fankt の派生ロジック（D7）がアプリ側と乖離する** → 意図した乖離である。fankt が `Embed.url` の対応サービスを増やしてもアプリは追随しない。追随が要る場合は変換層ではなくアプリ側のモデルを直す

## Open Questions

なし。issue #137 の未決事項 3 件（変換層の置き場所 / ID の表現 / 段階移行の可否）は D4 / D2 / D10 で解決した。
