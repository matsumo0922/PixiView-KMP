## Why

fankt の list API は `onItemSchemaMismatch` callback 付き overload を持つが、PixiView はどれも使っていない。

このため 2 つの問題がある。

1. `getSupportedPlans()` は callback 無しの版が strict であり、支援プランが 1 件でもデコードできないと支援プラン一覧が全体エラーになる。他の 9 系統は callback 無しでも壊れた項目をスキップするが、`getSupportedPlans()` だけは例外である。
2. 壊れた項目のスキップを PixiView 側で把握できない。fankt は診断ログを出すが、それは fankt の内部ログであり、Crashlytics には残らない。

epic #122 のゴール「1 件の異常でリスト全体が死なない」と「FANBOX API の変更時に原因を数十分で特定できる」は、いずれもこの callback を採用しなければ達成できない。

## What Changes

- `FanboxRepositoryImpl` の list API 10 メソッドを、fankt の `onItemSchemaMismatch` callback 付き overload の呼び出しに置き換える
- callback で受け取った `FanboxListItemSchemaMismatch` を `Napier.w` と `recordException` に流す private メソッドを `FanboxRepositoryImpl` に置く。`FanboxListItemSchemaMismatch` は `endpoint` と `indexPath` しか持たないため、PixiView が記録する内容に FANBOX / 利用者のデータは含まれない
- `getSupportedPlans()` の失敗挙動が変わる。スキーマ不一致で全体失敗していたものが、デコードできた分だけを返す部分成功になる。UI には従来どおりプラン一覧をそのまま表示し、スキップした事実は表示しない
- fankt の `logLevel` をデバッグビルドのみ `INFO`、リリースビルドでは `NONE` にする。fankt は `logLevel != NONE` のとき、スキーマ不一致の item の JSON を Napier へ出力する。credential は redact されるが FANBOX / 利用者のデータは残るため、リリースビルドでは出力しない
- 対象外: `FanboxException` の subtype に応じたエラー表示、再ログイン導線、`RateLimited` のリトライ制御。後続 change `classify-fanbox-errors` で扱う
- 対象外: callback overload が存在しない strict なエンドポイント。fankt 0.1.1 に緩和手段が無い

## Capabilities

### New Capabilities

- `fanbox-list-tolerance`: FANBOX の list API がスキーマ不一致の項目を含む場合に、残りの項目を返して処理を継続する振る舞い
- `fanbox-schema-diagnostics`: スキーマ不一致の発生を、エンドポイントと位置の情報とともに観測可能にする振る舞い

### Modified Capabilities

なし。`openspec/specs/` に既存 spec は存在しない。

## Impact

### 変更するコード

- `core/repository/src/commonMain/kotlin/me/matsumo/fanbox/core/repository/FanboxRepository.kt` — list API 10 メソッドの実装、mismatch の記録メソッド、`Fanbox` 生成時の `logLevel`
- `core/common/src/commonMain/kotlin/me/matsumo/fanbox/core/common/PixiViewConfig.kt` — `isDebug` の追加
- `shared/src/androidMain/kotlin/me/matsumo/fanbox/di/AppModule.android.kt` / `shared/src/iosMain/kotlin/me/matsumo/fanbox/di/AppModule.ios.kt` — `isDebug` の供給
- `core/repository/src/commonMain/kotlin/me/matsumo/fanbox/core/repository/di/RepositoryModule.kt` — `PixiViewConfig` の注入

### 変更しない箇所

- `ScreenState` / `ErrorView` / `AsyncLoadContents` — UI 層には触れない
- PagingSource 群と ViewModel — repository のシグネチャが変わらないため変更不要

### 依存関係

- fankt 0.1.1（導入済み）

### ドキュメント影響

あり。mismatch 記録メソッドの KDoc に記録内容と `rawBody` を含めない旨を記述する。新規ドキュメントファイルは作成しない。
