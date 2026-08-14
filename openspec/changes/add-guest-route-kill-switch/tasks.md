# Tasks

## 1. 停止スイッチの参照を実装する

- [ ] 1.1 `FanboxFactory.android.kt` に停止フラグのキーと取得間隔の定数を追加する
- [ ] 1.2 Remote Config を参照する private 関数を追加する。`setConfigSettingsAsync` の完了後に `fetchAndActivate` を開始し、活性化済みの値を `getBoolean` で読む。参照全体を `runCatching` で包み、失敗時は `recordException` で記録したうえで `false`（guest を起動する）へ倒す。`recordException` の呼び出し自体も `runCatching` で守る（`Firebase.crashlytics` の解決が同じ理由で失敗しうるため）
- [ ] 1.3 `createFanbox` を、停止フラグが立っている場合に配信先と公開鍵を渡さない分岐に変える

## 2. ドキュメントを更新する

- [ ] 2.1 README の `Remote parsing updates` に停止スイッチを追記する。停止できること、既定は guest を起動する側であること、反映までに取得間隔と次回起動を要すること
- [ ] 2.2 `docs/` と README を `guest` / `Remote Config` / `kill switch` で grep し、誤りになった記述が無いことを確認する

## 3. 検証する

- [ ] 3.1 `./gradlew :core:repository:testAndroidHostTest :core:repository:detekt` を通す（AGP 9 の KMP では `test` ではなくこのタスク名）
- [ ] 3.2 リリースビルドが通ることを確認する
- [ ] 3.3 Firebase コンソールに `android_guest_route_kill_switch` を boolean として作成する（リポジトリ外の作業。人間が行う）
- [ ] 3.4 実機で、フラグを `true` へ倒して再起動すると guest が起動しないこと、`false` へ戻して再起動すると起動することを確認する（人間が行う）
- [ ] 3.5 実機で、新規インストール直後の初回起動（Remote Config を一度も取得していない状態）で guest が起動することを確認する。design.md D3 が依拠する「値の無い boolean は `false` を返す」の成立確認（人間が行う）
