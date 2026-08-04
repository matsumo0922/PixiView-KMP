# fanbox-schema-diagnostics Specification

## Purpose
TBD - created by archiving change adopt-tolerant-list-callbacks. Update Purpose after archive.
## Requirements
### Requirement: スキーマ不一致の発生を観測できる

FANBOX の list API でスキーマ不一致により項目がスキップされたとき、アプリはその事実をエンドポイントと項目位置とともに記録しなければならない（MUST）。

#### Scenario: ログ出力が有効な環境での出力

- **WHEN** ログ出力が有効な環境（Android のデバッグビルド、または iOS）で list API の取得でスキーマ不一致の項目がスキップされる
- **THEN** アプリは警告レベルのログを出力する
- **AND** ログにはエンドポイント識別子と項目の位置（`indexPath`）が含まれる

#### Scenario: Android でのクラッシュレポート基盤への記録

- **WHEN** Android で list API の取得でスキーマ不一致の項目がスキップされる
- **THEN** アプリはクラッシュレポート基盤に非致命的な例外として記録する
- **AND** 記録にはエンドポイント識別子と項目の位置が含まれる

#### Scenario: 複数項目が同時にスキップされる

- **WHEN** 1 回の list API 取得で複数の項目がスキーマ不一致によりスキップされる
- **THEN** アプリはスキップされた項目ごとに記録を行う

### Requirement: クラッシュレポート基盤へ送る内容に FANBOX と利用者のデータを含めない

アプリがスキーマ不一致としてクラッシュレポート基盤へ送る内容は、エンドポイント識別子と項目位置のみでなければならない（MUST）。FANBOX の応答本文、認証情報、利用者に紐づく識別子を含めてはならない（MUST NOT）。

#### Scenario: 送信内容の制限

- **WHEN** アプリがスキーマ不一致をクラッシュレポート基盤へ記録する
- **THEN** 送信内容はエンドポイント識別子と項目位置に限られる

### Requirement: リリースビルドでは応答本文の断片を出力しない

リリースビルドでは、スキーマ不一致に伴う FANBOX 応答本文の断片を出力してはならない（MUST NOT）。デバッグビルドでは開発時の調査のために出力してよい。

#### Scenario: リリースビルド

- **WHEN** リリースビルドで list API の取得でスキーマ不一致の項目がスキップされる
- **THEN** FANBOX の応答本文の断片はログに出力されない

#### Scenario: デバッグビルド

- **WHEN** デバッグビルドで list API の取得でスキーマ不一致の項目がスキップされる
- **THEN** 調査に必要な応答本文の断片がログに出力される

