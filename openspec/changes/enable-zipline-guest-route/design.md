# 設計: 投稿詳細の取得に配信済み guest 経路を使う

## Context

`FanboxRepositoryImpl` は `core/repository` で `Fanbox` を 1 つ保持し、投稿詳細の取得を含む全 operation をそこへ委譲する（`FanboxRepository.kt:225`）。現在の生成は配信先を伴わないコンストラクタを呼ぶため、guest は起動しない。

fankt 側の状態は次のとおりである。

- `0.1.3` が Maven Central に公開済み
- 配信 manifest が `https://matsumo0922.github.io/fankt/zipline/v1/manifest.zipline.json` で稼働（9 モジュール、`fanboxGuest` 鍵で署名）
- host / guest の分離と退避判断は fankt の内側で完結する。呼び出し側は生成時に宣言するのみ

fankt は 3 段の経路を持つ。本 change では同梱 bundle を配置しないため、2 段目は不在である。

```
1. 配信 manifest から取得した bundle
2. アプリへ同梱した bundle        ← 本 change では不在（#140）
3. 直接経路（従来どおり）
```

## Goals / Non-Goals

### Goals

- 配信先へ到達できる場合に `post.info` を guest で処理する
- 配信先へ到達できない場合も投稿詳細の取得を成立させる
- guest 経路の失敗が既存のエラー分類を変えないことを確かめる

### Non-Goals

- Remote Config による kill switch（#139）
- 同梱 fallback bundle（#140）
- `post.info` 以外の operation の guest 化
- 配信基盤そのもの（fankt 側で完結済み）

## Decisions

### D1: 配信先と公開鍵を定数として持ち、BuildKonfig を経由しない（agent 仮決め）

manifest URL、鍵名、Ed25519 公開鍵はいずれも秘匿を要さず、ビルド種別や環境で値が変わらない。`BuildKonfig` は `local.properties` と環境変数から値を注入する仕組みであり、変わらない値をそこへ通すと `PixiViewConfig` の項目、Android と iOS 双方の `AppModule`、`local.properties` の記述が増えるだけで、得られる可変性に用途がない。

代替として検討した `BuildKonfig` 経由は、staging 用の配信先を分けたくなった時点で導入すれば足りる。現時点でその要件はない。

### D2: 定数は `Fanbox` を生成する箇所に置く（agent 仮決め）

参照箇所が 1 つであり、他モジュールから使う予定がないため、生成箇所と同じファイルに `private` の定数として置く。共有のための新しい型やモジュールを作らない。

### D3: 公開鍵は hex 文字列として持ち、復号して渡す（agent 仮決め）

`Fanbox` が受け取るのは `ByteArray` である。鍵の実値は 32 バイトであり、`byteArrayOf` の羅列は差し替え時に誤りが入りやすい。Kotlin 2.4.0 の `String.hexToByteArray()`（`@ExperimentalStdlibApi`）で復号する。stdlib で足りるため独自の復号処理は書かない。

### D4: リリースビルドでも guest を有効にする（ユーザー確認済み・issue の受け入れ条件）

issue の受け入れ条件が Android / iOS の実機動作とリリースビルドの成立を求めているため、ビルド種別で分岐させない。

### D5: 配信の停止はサーバ側の操作で行う（ユーザー確認済み）

本 change の期間中、配信を止める手段は fankt 側の操作に限る。manifest の巻き戻し、`gh-pages` からの削除、`GuestParseResult.GuestFailure` を返す bundle の配信のいずれかを使う。アプリ内の停止手段は #139 で追加する。

同梱 bundle を配置しないため、manifest の削除は直接経路への退避として機能する。#140 を実施すると、この手段は同梱 bundle の実行へ変わる。

### D6: guest 経路の失敗を既存のエラー分類へ委ねる（実コードで確認済み）

fankt は guest のスキーマ不一致を `FanboxExceptionFactory.schemaMismatch` として送出する（`FanboxGuestHost.kt:215`）。PixiView は `FanboxException.SchemaMismatch` を `FanboxErrorKind.SchemaMismatch` へ対応付け済みである（`FanboxErrorKind.kt:63`）。したがって分類と表示に手を入れない。

guest 自体の失敗（`GuestParseResult.GuestFailure`）は、fankt が当該インスタンスの残りの生存期間について guest の使用を止め、直接経路へ退避する。呼び出し側へは通常の結果として返るため、PixiView での対応を要しない。

## Risks / Trade-offs

**公開鍵を誤って埋め込むと、guest が一度も起動しないまま直接経路で動作し続ける** → 症状が「従来どおり動く」であり、無言で機能が死ぬ。埋め込んだ鍵が配信中の manifest の署名を検証できることを、実装時に実際の manifest に対して確認する。

**公開鍵はアプリ更新なしに変更できない** → fankt の `ManifestVerifier` は認識できる鍵名の最初の署名で検証するため、移行期に新旧両方の鍵で署名すれば、いずれの鍵を埋め込んだアプリも受け入れる。旧鍵の廃止は、それを埋め込んだリリースが使われなくなってからとなる。

**遠隔コードの実行がストアの規約に触れうる** → 用途を FANBOX の仕様変更への追従に限り、機能追加や目的変更に使わない（#104 の方針）。README に用途の限定を明記する。

**配信先へ到達できない利用者には配信済みの修正が届かない** → 本 change の受け入れ条件は「従来どおり表示される」ことであり、これは満たされる。修正を届け続けるには #140 を要する。

**配信先へ到達できない経路で最初の投稿詳細の表示が待たされる** → fankt は manifest の取得に明示的なタイムアウトを設定していない（`HttpTimeout` は fankt 全体に存在せず、guest 用クライアントの設定は `expectSuccess` と `followRedirects` のみ。`Fanbox.kt:209`）。応答が返らない経路ではプラットフォームの既定のタイムアウトまで待ってから直接経路へ移る。配信先が明示的に拒否を返す場合（404 など）は即座に退避するため、この遅延は「パケットが黙って落ちる」経路に限られる。

保証を「表示の成立」へ狭め、所要時間を対象外とした。タイムアウトの設定は fankt 側の変更を要するため、本 change では扱わず follow-up とする。

**guest の初回起動そのものが表示を遅らせうる** → 未計測。bundle の取得と QuickJS の起動に要する時間は実機で確認する。

**guest の診断が logLevel と無関係にログへ出る** → `FanboxDiagnosticSink` は `Napier.w` へ無条件に報告する（`Fanbox.kt:204`）。ただしこの経路は guest 固有ではなく、既に一覧系の repository へ渡されている（`Fanbox.kt:215-239`）。報告内容は `FanboxDiagnostics.sanitizeFragment` で認証情報を伏せたうえで 2048 文字に切り詰められ、応答本文の断片は `logLevel != NONE` の場合に限られる。本 change が導入する経路ではないため対処しない。

## Migration Plan

配信先へ到達できない場合の退避が fankt の内側にあるため、段階的な導入や feature flag を要しない。差し戻しは `Fanbox` の生成を元のコンストラクタへ戻すだけで足りる。

## Open Questions

- **Ed25519 公開鍵の実値**（高リスク・要人間確認）。fankt にコミットされておらず、秘密鍵は `ZIPLINE_SIGNING_PRIVATE_KEY_HEX` の repository secret にある。鍵ペアの生成時に出力された公開鍵を人間から受け取る必要がある。値が確定するまで実装を完了できない
- **Privacy Policy への記載の要否**（高リスク・要人間確認）。本リポジトリの外にあるため、判断と反映を人間へ引き継ぐ
- **実機での動作確認**。Android / iOS の実機確認は人間が行う
