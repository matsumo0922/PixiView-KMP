# fanbox-guest-route Specification

## Purpose
TBD - created by archiving change enable-zipline-guest-route. Update Purpose after archive.
## Requirements
### Requirement: 投稿詳細の取得に配信済み guest を使う

Android では、遠隔の停止フラグが立っていない限り、アプリは配信先の manifest URL、信頼する鍵名、Ed25519 公開鍵を伴って `Fanbox` を生成しなければならない（MUST）。これにより `post.info` の要求組み立てと応答解析が、配信された guest bundle で実行される。

配信先は 2 つのチャンネルに分かれており、アプリは `Setting.isDeveloperMode` によっていずれかを選ばなければならない（MUST）。有効なら dev チャンネルを、無効なら手動で昇格した検証済みのチャンネルを参照する。昇格の前に配信物を実機で確認する経路が、これによって成立する。ビルドの種別では分岐してはならない（MUST NOT）。

`Setting` は非同期にしか読めない保存先にあり、`Fanbox` の生成は同期的に起きる。アプリは保存済みの値を生成時に読まなければならず（MUST）、購読の状態に依存して既定値が返る読み方をしてはならない（MUST NOT）。

この読み取りは最初のコンポジションを行う thread をブロックするため、上限時間を設けなければならない（MUST）。読み取りに失敗した場合、および上限時間内に読み終えられなかった場合は、developer mode を無効とみなさなければならない（MUST）。未検証の配信物を実行しない側が fail-safe である。

この読み取りは、選択の結果を使う場合にだけ行わなければならない（MUST）。配信先を渡さないプラットフォーム、および停止フラグによって配信先を渡さない場合には、行ってはならない（MUST NOT）。読み取りは対価としてブロックを伴うため、選択が起きない経路に負わせない。

`Fanbox` はプロセスごとに一度だけ生成されるため、developer mode の変更は次回のアプリ起動から反映される。

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

#### Scenario: developer mode が無効なら昇格されたチャンネルを参照する

- **WHEN** developer mode が無効な状態で配信先を選ぶ
- **THEN** 手動で昇格した検証済みチャンネルの manifest URL が選ばれ、dev チャンネルの URL は選ばれない

#### Scenario: developer mode が有効なら dev チャンネルを参照する

- **WHEN** developer mode が有効な状態で配信先を選ぶ
- **THEN** dev チャンネルの manifest URL が選ばれる

#### Scenario: 保存済みの値を読めない

- **WHEN** `Fanbox` の生成時に保存済みの `Setting` を読み取れない
- **THEN** developer mode は無効とみなされ、昇格されたチャンネルが選ばれる

#### Scenario: 保存済みの値の読み取りが上限時間を超える

- **WHEN** `Fanbox` の生成時に保存済みの `Setting` の読み取りが上限時間内に終わらない
- **THEN** 読み取りは打ち切られ、developer mode は無効とみなされ、昇格されたチャンネルが選ばれる

#### Scenario: チャンネルの選択は停止フラグの判断を変えない

- **WHEN** developer mode の値によらず遠隔の停止フラグが立っている
- **THEN** アプリは配信先も公開鍵も渡さず、どちらのチャンネルへの取得も試みない

#### Scenario: 選択が起きない経路では読み取りも起きない

- **WHEN** iOS で `Fanbox` を生成する、または Android で遠隔の停止フラグが立った状態で `Fanbox` を生成する
- **THEN** 保存済みの `Setting` の読み取りは行われず、生成が読み取りでブロックされることもない

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

アプリが遠隔から解析処理を取得して実行することと、その用途、対象が Android であること、および遠隔から実行を停止できることを README に記載しなければならない（MUST）。停止スイッチの記載には、取得前および取得失敗時に guest を起動する側へ倒すこと、および停止が各端末へ届くまでに遅延があることを含めなければならない（MUST）。

取得されるのが、配信元へ最後に置かれたものではなく、手動で昇格された配信物であることを記載しなければならない（MUST）。取得される対象が誰の判断で変わるかは、遠隔実行の性質を読み取るうえで停止手段と同じ重みを持つ。

Privacy Policy は本リポジトリの外（`https://www.matsumo.me/application/pixiview/privacy_policy`）にあるため、本 change では変更できない。記載の要否の判断と反映は人間へ引き継ぐ。

#### Scenario: README を読む

- **WHEN** README を読む
- **THEN** 遠隔から解析処理を取得する旨、用途を FANBOX の仕様変更への追従に限る旨、対象が Android である旨、遠隔から実行を停止できる旨とその既定値・遅延、および取得されるのが手動で昇格された配信物である旨が記載されている

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
