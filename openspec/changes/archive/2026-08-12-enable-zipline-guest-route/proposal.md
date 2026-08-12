# 投稿詳細の取得に配信済み guest 経路を使う

## Why

FANBOX の応答スキーマが変わると投稿詳細の解析が壊れ、修正はストア審査を経たアプリ更新でしか利用者へ届かない。審査に要する日数のあいだ、投稿詳細は表示できないままになる。

fankt 0.1.2 以降は、署名した guest bundle を配信して解析処理を差し替える経路を備える。PixiView が `Fanbox` の生成時に配信先と信頼する公開鍵を宣言すると、解析の修正がアプリ更新を経ずに届く。宣言しない限り guest は起動せず、従来の直接経路のままである。

fankt 0.1.3 が Maven Central へ公開され、配信 manifest も稼働している。PixiView 側の宣言だけが欠けている。

## What Changes

- `gradle/libs.versions.toml` の `fankt` を `0.1.1` から `0.1.3` へ更新する
- `FanboxRepositoryImpl` の `Fanbox` 生成を、配信先と信頼する公開鍵を受け取るコンストラクタへ差し替える
- 配信先の manifest URL、信頼する鍵名、Ed25519 公開鍵を定数として持つ
- 遠隔から解析処理を取得することを README と Privacy Policy に記載する

破壊的変更はない。fankt の `v0.1.1` から `v0.1.3` の公開 ABI 差分は追加のみで、削除と変更を含まない。

## Capabilities

### New Capabilities

- `fanbox-guest-route`: 投稿詳細の取得に配信済み guest を使い、配信先へ到達できない場合も取得が成立することを定める

### Modified Capabilities

なし。guest 経路の失敗は直接経路と同じ `FanboxException` の subtype として現れるため、`fanbox-error-classification` の要件は変更を要しない。この同一性は本 change の非退行 invariant として検証する。

## Impact

### 変更するコード

- `gradle/libs.versions.toml`
- `core/repository/src/commonMain/kotlin/me/matsumo/fanbox/core/repository/FanboxRepository.kt`
- `README.md`
- Privacy Policy

### 依存

- `me.matsumo.fankt:fanbox` を `0.1.1` から `0.1.3` へ

### 実行時の挙動

`post.info` の解析経路が、配信先へ到達できる場合に guest へ移る。到達できない場合は従来の直接経路が使われる。他の operation は変更しない。

## 対象範囲

- 配信 manifest からの guest 起動
- 配信先へ到達できない場合の直接経路への退避
- 遠隔コード取得のドキュメント記載

## 非対象範囲

- Remote Config による kill switch（#139）
- アプリへ同梱する fallback bundle（#140）
- `post.info` 以外の operation の guest 化（matsumo0922/fankt#100）
- app-owned DTO 層（#137）

本 change の期間中、配信の停止は fankt 側のサーバ操作（manifest の巻き戻し、`gh-pages` からの削除、`GuestParseResult.GuestFailure` を返す bundle の配信）で行う。

## ドキュメント影響

あり。`README.md` と Privacy Policy に、遠隔から解析処理を取得する旨を記載する。
