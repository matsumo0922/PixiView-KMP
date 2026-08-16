## Why

matsumo0922/fankt#107 で guest bundle の配信が 2 チャンネルへ分かれた。`main` への push は `zipline/v1-dev` へ配信され、consumer が読む `zipline/v1` は手動の昇格でしか変わらない。

昇格の前に「dev で確認する」ことがこの構成の目的だが、PixiView 側に dev チャンネルを参照する手段が無い。確認する経路が存在しないまま昇格の関門だけがある状態になっている。

## What Changes

- `createFanbox` が `isDeveloperMode` を受け取り、有効なら dev チャンネル、無効なら昇格済みチャンネルの manifest URL を渡す。ビルドの種別では分岐しない
- `SettingDataStore` に、保存済みの `Setting` を一度読む suspend 関数を足す。`Setting.isDeveloperMode` は `SharingStarted.WhileSubscribed` の `StateFlow` にあり、購読が始まる前に `value` を読むと既定値が返るため、`Fanbox` の生成時点では現在の実装では読めない
- `FanboxRepositoryImpl` の生成時に一度だけその値を読み、`createFanbox` へ渡す
- 配信先を prod / dev の 2 つの定数に分ける
- iOS の `createFanbox` は引数を受け取るが使わない

## Capabilities

### New Capabilities

なし。

### Modified Capabilities

- `fanbox-guest-route`: 参照する配信先が developer mode によって異なることが要件になる。README への記載の要件にも、リリースが取得するのが昇格された配信物であることが加わる

## Impact

- `core/datastore/src/commonMain/.../SettingDataStore.kt`（保存済みの値を読む関数）
- `core/repository/src/commonMain/.../FanboxRepository.kt`（`expect fun createFanbox` の引数、生成時の読み取り）
- `core/repository/src/androidMain/.../FanboxFactory.android.kt`（配信先の定数と分岐）
- `core/repository/src/iosMain/.../FanboxFactory.ios.kt`（引数の追加のみ）
- `README.md` の `Remote parsing updates` 節
- developer mode が無効な場合の挙動は変わらない。参照先の URL は現在と同一である

## 配送形態

単一 PR とする。変更は 4 ファイルの小さな差分とドキュメントに閉じており、分割しても各 PR が独立してレビュー可能な単位にならない。

## 受け入れ条件との対応

issue #148 の受け入れ条件のうち、判断の入力は「ビルドの種別」から「developer mode」へ変更する（ユーザー確認済み）。issue 本文が `isDeveloperMode` を採らない理由として挙げた 2 点は、design.md の D2 と Risks で扱う。

- 「release ビルドが `zipline/v1` を参照する」→「developer mode が無効なら `zipline/v1` を参照する」と読み替える
- 「debug ビルドが `zipline/v1-dev` を参照する」→「developer mode が有効なら `zipline/v1-dev` を参照する」と読み替える
- 残る 4 件（停止フラグが両方で機能する / 実機確認 / リリースビルドが通る / README と delta spec の更新）はそのまま扱う
