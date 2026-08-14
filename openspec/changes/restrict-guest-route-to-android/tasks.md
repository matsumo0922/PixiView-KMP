# Tasks

## 1. 生成をプラットフォーム側へ委ねる

- [x] 1.1 `commonMain` に `createFanbox` の `expect` 宣言を置く
- [x] 1.2 `androidMain` に `actual` を置き、配信先・鍵名・公開鍵の定数を `FanboxRepository.kt` から移す
- [x] 1.3 `iosMain` に `actual` を置き、guest を伴わないコンストラクタで生成する
- [x] 1.4 `FanboxRepositoryImpl` の生成を `createFanbox` の呼び出しへ差し替える

## 2. 定数が iOS へ届かないことの確認

- [x] 2.1 `commonMain` と `iosMain` に配信先 URL・鍵名・公開鍵が残っていないことを確認する

## 3. ドキュメント

- [x] 3.1 `README.md` の「Remote parsing updates」に対象が Android である旨を加える

## 4. 検証

- [x] 4.1 `detekt` と単体テストを実行する
- [x] 4.2 Android のリリースビルドが通ることを確認する
- [x] 4.3 iOS 向けのコンパイルが通ることを確認する
