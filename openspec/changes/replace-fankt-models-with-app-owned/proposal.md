## Why

PixiView は fankt の domain model（`me.matsumo.fankt.fanbox.domain.model.*`）を `FanboxRepository` の public API、`feature/*`、`core/ui`、`core/model` へそのまま露出している。fankt のモデルが変わるたびに UI 層まで修正が波及し、fankt 0.1.0 への追従（#122）では #114 / #115 / #119 / #120 と UI 層に及ぶ追従 issue が 4 本立った。

アプリが所有するモデルへ変換する層を repository に置き、fankt のモデルをアプリの公開契約から外す。

## What Changes

- `core/model` に app-owned のモデル（`me.matsumo.fanbox.core.model.fanbox.*`）を定義する。fankt の domain model 24 ファイル / 749 行に対応する
- `core/repository` に fankt モデル ↔ app-owned モデルの変換層を置く
- **BREAKING**（アプリ内部の契約）：`FanboxRepository` の public API の型が app-owned モデルへ変わる。`feature/*`、`core/ui`、`core/model`、`shared` の呼び出し元をすべて追随させる
- `core/datastore` は変更しない。ブックマークの JSON 保存形式は fankt の `FanboxPost.serializer()` のままとし、変換は repository 層で行う

## Capabilities

### New Capabilities

- `app-owned-fanbox-models`: アプリが所有する FANBOX モデルと、fankt モデルとの変換層。アプリの公開契約から fankt の domain model を外すこと、変換が値を落とさないこと、既存の永続化データが読めることを規定する

### Modified Capabilities

なし。既存 spec（`fanbox-guest-route` / `fanbox-error-classification` / `fanbox-list-tolerance` / `fanbox-schema-diagnostics` / `fanbox-session-recovery`）が参照するのは `FanboxRepositoryImpl` / `Fanbox` / `FanboxException` であり、いずれも本変更の対象外である。

## Impact

- **新規**：`core/model/src/commonMain/.../core/model/fanbox/`（app-owned モデル）、`core/repository/src/commonMain/.../repository/mapper/`（変換層）
- **変更**：`core/repository`（`FanboxRepository` と実装、`paging/*` 5 本、`TranslationRepository`、`DownloadPostsRepository` の interface と android / ios 実装）、`feature/post` / `feature/creator` / `feature/library` / `core/ui` / `core/model` の既存ファイル / `feature/setting` / `shared`
- **変更しない**：`core/datastore`（11 ファイル）、`core/model/FanboxErrorKind.kt`、`feature/welcome/WebViewCookieConverter.kt`。cookie / session 系（`FanboxCookieStorage` / `FanboxCookieRecord` / `FanboxSessionId`）と例外（`FanboxException`）は domain model ではなく fankt が要求する契約であり、本 issue の対象外
- **依存**：fankt 0.1.3。バージョンは変えない
- **ドキュメント影響**：あり（`README.md` の Architecture 節、`core/model` と `core/repository` の責務記述）

## 受け入れ条件との対応

| issue #137 の受け入れ条件 | 対応 |
| --- | --- |
| `FanboxRepository` の public API、`feature/*`、`core/ui`、`core/model` に `me.matsumo.fankt` の型が現れない | この change |
| 永続化済みデータが移行後も読める（または migration が実装されている） | この change（保存形式を変えないことで満たす） |
| 既存のテストが通る | この change |
