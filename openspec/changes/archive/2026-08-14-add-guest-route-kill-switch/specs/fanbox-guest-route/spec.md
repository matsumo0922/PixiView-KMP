# fanbox-guest-route Delta

## MODIFIED Requirements

### Requirement: 投稿詳細の取得に配信済み guest を使う

Android では、遠隔の停止フラグが立っていない限り、アプリは配信先の manifest URL、信頼する鍵名、Ed25519 公開鍵を伴って `Fanbox` を生成しなければならない（MUST）。これにより `post.info` の要求組み立てと応答解析が、配信された guest bundle で実行される。

iOS では、アプリはこれらを伴わずに `Fanbox` を生成しなければならない（MUST）。guest は起動せず、`post.info` は組み込みの直接経路で処理される。

これらを伴わずに生成した `Fanbox` は guest を起動しないため、宣言そのものが本 capability の成立条件である。

配信先と鍵は Android の source set に置き、iOS のバイナリへ含めない。

#### Scenario: 配信先と公開鍵を伴って生成する

- **WHEN** Android で、遠隔の停止フラグが立っていない状態で `FanboxRepositoryImpl` が `Fanbox` を生成する
- **THEN** アプリは配信先の manifest URL、信頼する鍵名、Ed25519 公開鍵を渡す

#### Scenario: iOS で guest を起動しない

- **WHEN** iOS で `FanboxRepositoryImpl` が `Fanbox` を生成する
- **THEN** アプリは配信先も公開鍵も渡さず、`post.info` は直接経路で処理される

#### Scenario: 公開鍵が Ed25519 の鍵長を満たす

- **WHEN** 埋め込んだ公開鍵を復号する
- **THEN** 32 バイトになる

### Requirement: 遠隔からの解析処理の取得を README に記載する

アプリが遠隔から解析処理を取得して実行することと、その用途、対象が Android であること、および遠隔から実行を停止できることを README に記載しなければならない（MUST）。停止スイッチの記載には、取得前および取得失敗時に guest を起動する側へ倒すこと、および停止が各端末へ届くまでに遅延があることを含めなければならない（MUST）。

Privacy Policy は本リポジトリの外（`https://www.matsumo.me/application/pixiview/privacy_policy`）にあるため、本 change では変更できない。記載の要否の判断と反映は人間へ引き継ぐ。

#### Scenario: README を読む

- **WHEN** README を読む
- **THEN** 遠隔から解析処理を取得する旨、用途を FANBOX の仕様変更への追従に限る旨、対象が Android である旨、および遠隔から実行を停止できる旨とその既定値・遅延が記載されている

## ADDED Requirements

### Requirement: 配信済み guest の実行を遠隔から停止できる

Android では、アプリは Firebase Remote Config の boolean フラグ `android_guest_route_kill_switch` を参照し、値が `true` の場合は配信先と公開鍵を渡さずに `Fanbox` を生成しなければならない（MUST）。この場合 `post.info` は組み込みの直接経路で処理され、配信先への取得は試みられない。

これは配信元（manifest の巻き戻しや削除）とは独立した停止手段である。配信元が応答を返さない場合、配信元を操作できない場合、および配信物と無関係に遠隔コードの実行を止める必要が生じた場合に、アプリ側の判断だけで guest の起動を止める。

参照は `Fanbox` の生成時に一度だけ行い、活性化済みの値を同期的に読む。フラグの取得は背景で行い、取得した値は次回のアプリ起動時に反映される。したがって停止を決めてから端末へ届くまでには、取得間隔の経過と次回起動の両方を要する遅延がある。

活性化済みの値が無い場合、および参照が失敗した場合、アプリは guest を起動する側へ倒さなければならない（MUST）。フラグを「停止」の向きで定義することで、値が無い boolean に Remote Config が返す `false` がそのまま「停止していない」を意味する。これにより Remote Config へ到達できない端末でも配信済みの guest は成立し続ける。

参照が失敗した場合、アプリはその失敗を記録しなければならない（MUST）。この失敗の症状は「停止フラグを倒しても止まらない」であり、記録が無ければ観測できない。記録そのものが失敗した場合も、アプリはその失敗を伝播させてはならない（MUST NOT）。

#### Scenario: 停止フラグが立っている

- **WHEN** Android で `android_guest_route_kill_switch` が `true` として活性化されている状態で `Fanbox` を生成する
- **THEN** アプリは配信先も公開鍵も渡さず、`post.info` は直接経路で処理される

#### Scenario: 停止フラグを一度も取得できていない

- **WHEN** Android で Remote Config の活性化済みの値が無い状態で `Fanbox` を生成する
- **THEN** アプリは配信先と公開鍵を渡し、guest を起動する

#### Scenario: Remote Config の参照が失敗する

- **WHEN** Android で Remote Config の参照が例外を送出する
- **THEN** アプリはその例外を伝播させず、失敗を記録したうえで配信先と公開鍵を渡して guest を起動する

#### Scenario: 参照の失敗を記録できない

- **WHEN** Android で Remote Config の参照が失敗し、その失敗の記録もまた例外を送出する
- **THEN** アプリはいずれの例外も伝播させず、配信先と公開鍵を渡して guest を起動する

#### Scenario: 停止フラグを倒した後に再起動する

- **WHEN** 停止フラグを `true` へ倒し、取得間隔の経過後にアプリを再起動する
- **THEN** アプリは guest を起動しない
