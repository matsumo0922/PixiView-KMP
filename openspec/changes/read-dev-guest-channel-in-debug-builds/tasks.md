## 1. 保存済みの設定を読む

- [x] 1.1 `SettingDataStore` に、保存済みの `Setting` を一度読む suspend 関数を足す（`setting`（StateFlow）ではなく、その元になっている preferences の Flow を読むこと。StateFlow から読むと購読前は既定値が返る）

## 2. 配信先の選択

- [x] 2.1 `expect fun createFanbox` に `isDeveloperMode: Boolean` を足す
- [x] 2.2 `FanboxRepositoryImpl` の生成時に保存済みの値を一度読み、読めない場合と上限時間を超えた場合は無効として `createFanbox` へ渡す
- [x] 2.3 `FanboxFactory.android.kt` の配信先を prod / dev の 2 定数に分け、引数で選ぶ
- [x] 2.4 `FanboxFactory.ios.kt` の actual に引数を足す（値は使わない）

## 3. ドキュメント

- [x] 3.1 `README.md` の `Remote parsing updates` 節に、取得されるのが手動で昇格された配信物であることを記載する

## 4. 検証

- [x] 4.1 配信先の選択を検証する最小のテストを `androidHostTest` に置き、`testAndroidHostTest` で通す
- [x] 4.2 detekt とリリースビルドが通ることを確認する
- [x] 4.3 リリースの成果物に両チャンネルの URL が残っていることを確認する（選択は実行時に起きるため、R8 が dev 側の定数を落とすと developer mode が効かなくなる）
- [ ] 4.4 実機で、developer mode を有効にした状態で guest が退避せずに投稿詳細を取得できることを確認する
