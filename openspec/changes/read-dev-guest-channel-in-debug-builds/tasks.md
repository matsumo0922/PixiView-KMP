## 1. 配信先の選択

- [ ] 1.1 `expect fun createFanbox` に `isDebug: Boolean` を足し、`FanboxRepositoryImpl` の呼び出しで `pixiViewConfig.isDebug` を渡す
- [ ] 1.2 `FanboxFactory.android.kt` の配信先を prod / dev の 2 定数に分け、`isDebug` で選ぶ
- [ ] 1.3 `FanboxFactory.ios.kt` の actual に引数を足す（値は使わない）

## 2. ドキュメント

- [ ] 2.1 `README.md` の `Remote parsing updates` 節に、リリースが取得するのが手動で昇格された配信物であることを記載する

## 3. 検証

- [ ] 3.1 配信先の選択を検証する最小のテストを `androidHostTest` に置き、`testAndroidHostTest` で通す
- [ ] 3.2 detekt とリリースビルドが通ることを確認する
- [ ] 3.3 実機の debug ビルドで、guest が退避せずに投稿詳細を取得できることを確認する
