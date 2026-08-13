# 設計: guest 経路の有効化を Android 限定にする

## Context

`FanboxRepositoryImpl` は `core/repository/src/commonMain` にあり、`Fanbox` を 1 つ保持する。生成箇所は 1 箇所で、配信先・鍵名・公開鍵を渡す guest コンストラクタを呼んでいる。プラットフォームによる分岐は無い。

`core/repository` は `androidMain` と `iosMain` の source set を持ち、`di/RepositoryModule.kt` の `expect val repositorySubModule: Module` を各プラットフォームで `actual` として実装している。プラットフォーム差を表現する手段が既にある。

## Goals / Non-Goals

### Goals

- Android のみで guest を起動する
- 配信先と鍵を iOS のバイナリへ含めない
- 実行時のフラグを増やさない

### Non-Goals

- kill switch（#139）
- 同梱 fallback bundle（#140）
- iOS で guest を動かすための仕組み

## Decisions

### D1: `Fanbox` の生成を `expect` / `actual` の関数へ移す（agent 仮決め）

`Fanbox` は guest 用と直接経路用で別のコンストラクタを持つため、引数の有無ではなく呼び分けが要る。生成そのものをプラットフォーム側へ委ねる。

```kotlin
// commonMain
internal expect fun createFanbox(
    logLevel: FanboxLogLevel,
    ioDispatcher: CoroutineDispatcher,
    cookieStorage: FanboxCookieStorage,
): Fanbox
```

代替として検討した実行時フラグ（`PixiViewConfig` へ Boolean を足し、`if` で 2 つのコンストラクタを呼び分ける）は、共通の引数を 2 箇所へ書くことになり、かつ配信先と鍵が `commonMain` に残るため iOS のバイナリからも消えない。`expect` / `actual` はこの 2 点を同時に解決する。

同モジュールの `repositorySubModule` が既に同じ仕組みを使っており、新しい表現を持ち込まない。

### D2: 配信先・鍵名・公開鍵を `androidMain` へ移す（agent 仮決め）

iOS では参照しないため、`commonMain` に置く理由がない。Android の source set へ移すことで、iOS のバイナリへ含まれないことが配置から保証される。

公開鍵の定数を検証するテストは `androidHostTest` にあり、`androidMain` の `internal` を参照できるため、移動後もそのまま成立する。

### D3: iOS の挙動は #138 以前と同じにする（ユーザー確認済み）

iOS は guest を伴わないコンストラクタで `Fanbox` を生成する。`logLevel` / `ioDispatcher` / `cookieStorage` の渡し方は #138 以前と同じである。

## Risks / Trade-offs

**iOS に解析の修正が OTA で届かなくなる** → FANBOX の仕様変更時、iOS はストア審査を経た更新でのみ修正が届く。これは #138 以前と同じ状態であり、退行ではない。適用範囲を Android に限る方針に伴う既知の帰結である。

**プラットフォームで挙動が分かれる** → 同じ FANBOX の仕様変更に対し、Android は配信で直り iOS は直らない期間が生じる。障害調査の際、どちらのプラットフォームかで解析経路が異なることを踏まえる必要がある。

**`expect` / `actual` の追加でプラットフォーム側の実装が 2 つに増える** → 生成の引数が変わるたび 3 箇所（expect と actual 2 つ）を直す。呼び分けが必要である以上避けられず、同モジュールの既存の `repositorySubModule` と同じ構造に収まる。

## Migration Plan

差し戻しは `createFanbox` の iOS 実装を guest コンストラクタへ変え、定数を `commonMain` へ戻すだけで足りる。データの移行を伴わない。

## Open Questions

- **実機での動作確認**。Android で guest が動くこと、iOS で guest が起動しないことの確認は人間が行う
