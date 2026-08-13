# fanbox-guest-route

## ADDED Requirements

### Requirement: 投稿詳細の取得に配信済み guest を使う

アプリは、配信先の manifest URL、信頼する鍵名、Ed25519 公開鍵を伴って `Fanbox` を生成しなければならない（MUST）。これにより `post.info` の要求組み立てと応答解析が、配信された guest bundle で実行される。

これらを伴わずに生成した `Fanbox` は guest を起動しないため、宣言そのものが本 capability の成立条件である。

#### Scenario: 配信先と公開鍵を伴って生成する

- **WHEN** `FanboxRepositoryImpl` が `Fanbox` を生成する
- **THEN** アプリは配信先の manifest URL、信頼する鍵名、Ed25519 公開鍵を渡す

#### Scenario: 公開鍵が Ed25519 の鍵長を満たす

- **WHEN** 埋め込んだ公開鍵を復号する
- **THEN** 32 バイトになる

### Requirement: 配信先へ到達できなくても投稿詳細の取得は成立する

配信先へ到達できない場合、アプリは `post.info` を guest を経由しない直接経路で処理しなければならない（MUST）。投稿詳細は guest 導入前と同じ内容で表示される。

保証するのは表示の成立であり、所要時間は含まない。配信先への到達を待つ分の遅延が最初の 1 回に生じる。応答が返らない経路（配信先へのパケットが黙って落ちる状態）では、HTTP クライアントの既定のタイムアウトまで待ってから直接経路へ移る。fankt は manifest の取得に明示的なタイムアウトを設定していないため、待ち時間はプラットフォームの既定値に従う。

同梱 bundle を持たないため、配信先へ到達できない状態では配信済みの修正は適用されない。

#### Scenario: 配信先へ到達できない

- **WHEN** 配信先の manifest を取得できない状態で投稿詳細を開く
- **THEN** アプリは直接経路で解析し、投稿詳細を表示する

#### Scenario: 署名を検証できない bundle が配信されている

- **WHEN** 信頼する公開鍵で検証できない bundle が配信先に置かれている
- **THEN** アプリはその bundle を実行せず、直接経路で解析する

### Requirement: guest 経路の失敗は既存のエラー分類を変えない

guest 経路で応答の形式が期待と異なる場合、アプリはその失敗を直接経路と同じ種別へ分類しなければならない（MUST）。fankt は guest のスキーマ不一致を `FanboxException.SchemaMismatch` として送出するため、`fanbox-error-classification` の分類と表示は変更を要しない。

#### Scenario: guest 経路でスキーマ不一致が起きる

- **WHEN** guest が応答を解釈できず、`FanboxException.SchemaMismatch` が送出される
- **THEN** アプリは失敗をスキーマ不一致として分類する

#### Scenario: FANBOX 由来でない失敗が起きる

- **WHEN** `FanboxException` でない例外が送出される
- **THEN** アプリは失敗をその他として分類する

### Requirement: 遠隔からの解析処理の取得を README に記載する

アプリが遠隔から解析処理を取得して実行することと、その用途を README に記載しなければならない（MUST）。

Privacy Policy は本リポジトリの外（`https://www.matsumo.me/application/pixiview/privacy_policy`）にあるため、本 change では変更できない。記載の要否の判断と反映は人間へ引き継ぐ。

#### Scenario: README を読む

- **WHEN** README を読む
- **THEN** 遠隔から解析処理を取得する旨と、用途を FANBOX の仕様変更への追従に限る旨が記載されている
