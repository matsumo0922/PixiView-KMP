## Why

fankt 0.1.1 の `FanboxException` は 8 種類の subtype を持ち、`statusCode` / `endpoint` / `rawBody` を伴う。しかし PixiView は `suspendRunCatching` ですべての `Throwable` を一括で捕捉し、`ScreenState.Error(Res.string.error_network)` に潰している。`ScreenState.Error` は `StringResource` しか持てないため、原因の情報は UI へ渡る前に完全に失われる。

この結果、次の 3 つが起きている。

1. セッションが切れて 401 が返っても、画面には「通信エラーが発生しました」とだけ出る。利用者は再ログインが必要だと気付けない。復帰は最大 10 分後の疎通ポーリング（`PixiViewViewModel.updateState()`）を待つしかない
2. 429 でレート制限されても、利用者は「何度も再試行ボタンを押す」以外の行動を取れない
3. ネットワーク断とサーバ障害とスキーマ不一致がすべて同じ文言になり、利用者も開発者も切り分けられない

epic #122 のゴール「FANBOX API の変更時に原因を数十分で特定できる」は、この分類を通さない限り達成できない。

## What Changes

- `FanboxException` を UI が扱える分類（`FanboxErrorKind`）へ変換する分類器を `core:model` に新設する
- FANBOX API の失敗から `ScreenState.Error` を作る共通ファクトリを `core:model` に置き、ViewModel 12 箇所をそれ経由に置き換える
- 分類ごとにエラー文言を切り替える。`RateLimited` は待機時間を表示せず、しばらく待つよう促す固定文言とする
- 401 を検知したとき、`FanboxRepository` からセッション無効化を通知し、`PixiViewViewModel` が welcome フローへ差し替える。既存の `logoutTrigger` と同じ機構を用いる
- 対象外: tolerant list callback の採用とスキーマ不一致の観測。先行 change `adopt-tolerant-list-callbacks` の範囲
- 対象外: 429 の自動リトライやバックオフ

## Capabilities

### New Capabilities

- `fanbox-error-classification`: FANBOX API の失敗を種別ごとに分類し、種別に応じた文言を表示する振る舞い
- `fanbox-session-recovery`: セッションが無効になったことを検知し、利用者を再ログインへ導く振る舞い

### Modified Capabilities

なし。`openspec/specs/` に既存 spec は存在しない。

## Impact

### 変更するコード

- `core/model` に `FanboxErrorKind` と分類器、`ScreenState.Error` の共通ファクトリを新設。`ScreenState.kt` 自体は変更しない。`core:model` は `libs.fankt.fanbox` を `api` で持つため fankt の型を直接扱える
- `core/repository/src/commonMain/kotlin/me/matsumo/fanbox/core/repository/FanboxRepository.kt` — 401 検知とセッション無効化の通知
- `shared/src/commonMain/kotlin/me/matsumo/fanbox/PixiViewViewModel.kt` — セッション無効化の購読
- `core/resources/src/commonMain/composeResources/values/strings.xml` — 分類ごとの文言を追加。翻訳は既定の英語へフォールバックさせ、他ロケールへの追加は行わない
- ViewModel 12 箇所 — `ScreenState.Error` の生成を共通ファクトリ経由に置き換え

### 変更しない箇所

- `ScreenState` / `AsyncLoadContents` / `ErrorView` — いずれも無改修
- PagingSource 群と `PagingErrorSection` — Paging のエラー表示は `LoadState.Error` 経由であり `ScreenState` を通らない
- `BillingViewModel` — FANBOX 由来でないため分類を通さない

### 依存関係

- fankt 0.1.1（導入済み）
- **先行 change `adopt-tolerant-list-callbacks` に依存する。** 両者は `FanboxRepository.kt` の同じメソッド本体を書き換えるため、adopt の完了後に着手する

### ドキュメント影響

あり。`FanboxErrorKind` の KDoc に、各分類がどの `FanboxException` subtype に対応するかを記述する。新規ドキュメントファイルは作成しない。
