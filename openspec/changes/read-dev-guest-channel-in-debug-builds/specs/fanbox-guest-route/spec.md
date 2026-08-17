## MODIFIED Requirements

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

### Requirement: 遠隔からの解析処理の取得を README に記載する

アプリが遠隔から解析処理を取得して実行することと、その用途、対象が Android であること、および遠隔から実行を停止できることを README に記載しなければならない（MUST）。停止スイッチの記載には、取得前および取得失敗時に guest を起動する側へ倒すこと、および停止が各端末へ届くまでに遅延があることを含めなければならない（MUST）。

取得されるのが、配信元へ最後に置かれたものではなく、手動で昇格された配信物であることを記載しなければならない（MUST）。取得される対象が誰の判断で変わるかは、遠隔実行の性質を読み取るうえで停止手段と同じ重みを持つ。

Privacy Policy は本リポジトリの外（`https://www.matsumo.me/application/pixiview/privacy_policy`）にあるため、本 change では変更できない。記載の要否の判断と反映は人間へ引き継ぐ。

#### Scenario: README を読む

- **WHEN** README を読む
- **THEN** 遠隔から解析処理を取得する旨、用途を FANBOX の仕様変更への追従に限る旨、対象が Android である旨、遠隔から実行を停止できる旨とその既定値・遅延、および取得されるのが手動で昇格された配信物である旨が記載されている
