## Why

matsumo0922/fankt#107 で guest bundle の配信が 2 チャンネルへ分かれた。`main` への push は `zipline/v1-dev` へ配信され、consumer が読む `zipline/v1` は手動の昇格でしか変わらない。

昇格の前に「dev で確認する」ことがこの構成の目的だが、PixiView 側に dev チャンネルを参照する手段が無い。確認する経路が存在しないまま昇格の関門だけがある状態になっている。

## What Changes

- `createFanbox` が `isDebug` を受け取り、debug ビルドでは dev チャンネル、それ以外では検証済みチャンネルの manifest URL を渡す
- 配信先を prod / dev の 2 つの定数に分ける
- iOS の `createFanbox` は引数を受け取るが使わない。iOS は配信先を渡さないため

## Capabilities

### New Capabilities

なし。

### Modified Capabilities

- `fanbox-guest-route`: 参照する配信先がビルドの種別によって異なることが要件になる。README への記載の要件にも、リリースが読むのが昇格された bundle であることが加わる

## Impact

- `core/repository/src/commonMain/.../FanboxRepository.kt`（`expect fun createFanbox` の引数、呼び出し）
- `core/repository/src/androidMain/.../FanboxFactory.android.kt`（配信先の定数と分岐）
- `core/repository/src/iosMain/.../FanboxFactory.ios.kt`（引数の追加のみ）
- `README.md` の `Remote parsing updates` 節
- リリースビルドの挙動は変わらない。参照先の URL は現在と同一である

## 配送形態

単一 PR とする。変更は 3 ファイルの小さな差分とドキュメントに閉じており、分割しても各 PR が独立してレビュー可能な単位にならない。

## 受け入れ条件との対応

issue #148 の受け入れ条件 6 件はすべて本 PR で扱う。範囲外として issue が挙げた 2 点（fankt 側のチャンネル分離、`isDeveloperMode` による実行時の切り替え）は含めない。
