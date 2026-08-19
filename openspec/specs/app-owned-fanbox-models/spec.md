# app-owned-fanbox-models Specification

## Purpose
アプリが所有する FANBOX のモデルと、fankt との境界を規定する。画面と repository の公開契約に fankt の domain model が現れないこと、境界の変換が値を落とさないこと、永続化済みのブックマークとブロック設定が読めることを扱う。
## Requirements
### Requirement: アプリの公開契約は fankt の domain model を露出しない

`FanboxRepository` の public API、`feature/*`、`core/ui`、`core/model`、`shared` は、fankt の domain model（`me.matsumo.fankt.fanbox.domain.*`）を参照してはならない（MUST NOT）。これらの層が扱う FANBOX のモデルは、アプリが所有するもの（`me.matsumo.fanbox.core.model.fanbox.*`）に限る。

fankt の domain model を扱ってよいのは `core/repository`（変換層とその呼び出し元）と `core/datastore`（ブックマークの保存形式）に限る。

domain model ではない fankt の契約は、この要件の対象ではなく層を問わず参照してよい。例外（`FanboxException`）、cookie と session（`FanboxCookieRecord` / `FanboxCookieStorage` / `FanboxSessionId`）、エントリポイント（`Fanbox` / `FanboxLogLevel`）、スキーマ不一致の診断（`FanboxListItemSchemaMismatch`）が該当する。

#### Scenario: リポジトリの公開宣言に domain model が現れない

- **WHEN** `FanboxRepository` の interface 宣言を読む
- **THEN** FANBOX のモデルを表す引数・戻り値・プロパティはすべてアプリ所有のモデルであり、fankt の型が残るのは cookie を渡す引数だけである

#### Scenario: UI 層から呼ばれる他のリポジトリも domain model を要求しない

- **WHEN** `feature/*` から呼ばれる `TranslationRepository` と `DownloadPostsRepository` の公開宣言を読む
- **THEN** 引数と戻り値の型はすべてアプリ所有のモデルか標準ライブラリの型である

#### Scenario: UI 層のソースに fankt の import が現れない

- **WHEN** `feature/*`、`core/ui`、`core/model`、`shared` のソースを走査する
- **THEN** `me.matsumo.fankt.fanbox.domain` から始まる import は 1 件も存在しない

#### Scenario: 例外と診断の契約は対象外である

- **WHEN** `FanboxException` / `FanboxErrorKind` / `FanboxListItemSchemaMismatch` の扱いを確認する
- **THEN** これらは domain model ではないため置き換えの対象外であり、`fanbox-error-classification` と `fanbox-schema-diagnostics` の既存要件は変更されない

### Requirement: 変換は fankt のモデルの値を落とさない

変換層は fankt の domain model からアプリ所有のモデルへ、すべてのフィールドを写さなければならない（MUST）。sealed 階層はすべての variant を写し、未知を表す variant が保持する生の JSON も保持しなければならない（MUST）。

#### Scenario: 投稿の全フィールドが写る

- **WHEN** すべてのフィールドに値を持つ fankt の投稿モデルを変換する
- **THEN** アプリ所有の投稿モデルの各フィールドが、対応する fankt のフィールドと同じ値を持つ

#### Scenario: 投稿詳細の本文の全 variant が写る

- **WHEN** 記事 / 画像 / ファイル / テキスト / 動画 / HTML / 未知 のそれぞれを本文に持つ投稿詳細を変換する
- **THEN** いずれも対応するアプリ所有の variant へ変換され、変換に失敗するものはない

#### Scenario: 記事ブロックの全 variant が写る

- **WHEN** テキスト / 画像 / ファイル / リンク / 埋め込み / 未知 のブロックを含む記事本文を変換する
- **THEN** いずれも対応するアプリ所有のブロックへ変換され、テキストブロックが持つ装飾範囲とリンク範囲も写る

#### Scenario: 未知の内容は生の JSON を保ったまま写る

- **WHEN** 未知の本文種別、または未知の記事ブロックを含む投稿詳細を変換する
- **THEN** アプリ所有のモデルは元の生の JSON 文字列を同じ値で保持する

### Requirement: アプリから fankt へ戻す変換は往復で元の値へ戻る

アプリ所有のモデルのうち、fankt へ渡す必要があるもの（投稿 ID / クリエイター ID / ユーザー ID / コメント ID / 投稿 / ページングのカーソル）は、fankt のモデルへ戻す変換を持たなければならない（MUST）。往復した値は元の値と等しくなければならない（MUST）。

#### Scenario: ID が往復する

- **WHEN** fankt の各 ID をアプリ所有の ID へ変換し、再び fankt の ID へ戻す
- **THEN** 戻した値は元の値と等しい

#### Scenario: 投稿が往復する

- **WHEN** すべてのフィールドに値を持つ fankt の投稿をアプリ所有の投稿へ変換し、再び fankt の投稿へ戻す
- **THEN** 戻した値は元の値と等しい

#### Scenario: ページングのカーソルが往復する

- **WHEN** fankt のカーソルをアプリ所有のカーソルへ変換し、再び fankt のカーソルへ戻す
- **THEN** 戻した値は元の値と等しい

### Requirement: 派生プロパティはアプリ所有のモデルでも同じ値を返す

fankt のモデルが計算していた派生プロパティ（投稿詳細のブラウザ URL、本文が持つ画像とファイルの一覧、埋め込みと動画の URL、ファイルの画像 / 動画への読み替え）は、アプリ所有のモデルでも同じ入力に対して同じ値を返さなければならない（MUST）。

#### Scenario: 投稿詳細のブラウザ URL が変わらない

- **WHEN** 同じ投稿詳細について fankt のモデルとアプリ所有のモデルのブラウザ URL を比べる
- **THEN** 両者は等しい

#### Scenario: 埋め込みと動画の URL が変わらない

- **WHEN** 対応済みのサービス提供者を持つ埋め込みブロックと動画本文について、両モデルの URL を比べる
- **THEN** 両者は等しく、未対応の提供者ではどちらも URL を返さない

#### Scenario: ファイルの読み替えが変わらない

- **WHEN** 画像の拡張子を持つファイル、動画の拡張子を持つファイル、どちらでもないファイルについて、両モデルの読み替え結果を比べる
- **THEN** 変換できる場合は同じ値を返し、変換できない場合はどちらも値を返さない

### Requirement: 永続化済みのデータは置き換え後も読める

ブックマークとブロック設定の保存形式は変更してはならない（MUST NOT）。アプリ所有のモデルへの置き換え前に保存されたデータは、置き換え後も同じ内容として読めなければならない（MUST）。

#### Scenario: 保存済みのブックマークが読める

- **WHEN** 置き換え前の形式で保存されたブックマークの JSON を読み出す
- **THEN** アプリ所有の投稿モデルとして復元され、各フィールドが保存時の値と一致する

#### Scenario: ブックマークの保存形式が変わらない

- **WHEN** アプリ所有の投稿モデルをブックマークとして保存する
- **THEN** 保存される JSON は置き換え前と同じ形式であり、置き換え前のアプリでも読める

#### Scenario: ブロック済みクリエイターが読める

- **WHEN** 置き換え前に保存されたブロック済みクリエイターの一覧を読み出す
- **THEN** アプリ所有のクリエイター ID の集合として復元され、内容が保存時と一致する

#### Scenario: 画面遷移の引数の表現が変わらない

- **WHEN** 投稿 ID とクリエイター ID を画面遷移の引数として符号化する
- **THEN** 符号化された文字列は置き換え前と同じであり、既存のリンクがそのまま解決できる
