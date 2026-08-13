# fanbox-guest-route

## MODIFIED Requirements

### Requirement: 投稿詳細の取得に配信済み guest を使う

Android では、アプリは配信先の manifest URL、信頼する鍵名、Ed25519 公開鍵を伴って `Fanbox` を生成しなければならない（MUST）。これにより `post.info` の要求組み立てと応答解析が、配信された guest bundle で実行される。

iOS では、アプリはこれらを伴わずに `Fanbox` を生成しなければならない（MUST）。guest は起動せず、`post.info` は組み込みの直接経路で処理される。

これらを伴わずに生成した `Fanbox` は guest を起動しないため、宣言そのものが本 capability の成立条件である。

配信先と鍵は Android の source set に置き、iOS のバイナリへ含めない。

#### Scenario: Android で配信先と公開鍵を伴って生成する

- **WHEN** Android で `FanboxRepositoryImpl` が `Fanbox` を生成する
- **THEN** アプリは配信先の manifest URL、信頼する鍵名、Ed25519 公開鍵を渡す

#### Scenario: iOS で guest を起動しない

- **WHEN** iOS で `FanboxRepositoryImpl` が `Fanbox` を生成する
- **THEN** アプリは配信先も公開鍵も渡さず、`post.info` は直接経路で処理される

#### Scenario: 公開鍵が Ed25519 の鍵長を満たす

- **WHEN** 埋め込んだ公開鍵を復号する
- **THEN** 32 バイトになる

### Requirement: 遠隔からの解析処理の取得を README に記載する

アプリが遠隔から解析処理を取得して実行することと、その用途、および対象が Android であることを README に記載しなければならない（MUST）。

Privacy Policy は本リポジトリの外（`https://www.matsumo.me/application/pixiview/privacy_policy`）にあるため、本 change では変更できない。記載の要否の判断と反映は人間へ引き継ぐ。

#### Scenario: README を読む

- **WHEN** README を読む
- **THEN** 遠隔から解析処理を取得する旨、用途を FANBOX の仕様変更への追従に限る旨、および対象が Android である旨が記載されている
