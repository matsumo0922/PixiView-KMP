## Context

現在のエラー経路は 3 段で情報を失う。

1. `suspendRunCatching`（`core/common/src/commonMain/kotlin/me/matsumo/fanbox/core/common/util/CoroutineUtils.kt:9-19`）が `CancellationException` 以外の `Throwable` を無差別に `Result.failure` へ包む
2. ViewModel が `.fold(onFailure = { ScreenState.Error(Res.string.error_network) })` と書き、`it` を捨てる
3. `ScreenState.Error`（`core/model/src/commonMain/kotlin/me/matsumo/fanbox/core/model/ScreenState.kt:12-15`）は `message` と `retryTitle` しか持てない

`Throwable` は 2 段目で消える。したがって 2 段目で分類して文言を決めれば、3 段目の構造を変えずに要件を満たせる。

fankt 側の事実は次のとおりである。

- `FanboxException` は sealed class で、**8 subtype すべてのコンストラクタが `internal`**（`FanboxException.kt:27-89`）。ABI dump にも public constructor は無い。`Forbidden` は `open` だがコンストラクタが internal のため subclass も作れない。**テストから subtype のインスタンスを生成する手段が無い。**
- base クラスは `statusCode: Int?` / `rawBody: String?` / `endpoint: String` を public で持つ（`FanboxException.kt:18-24`）
- `FanboxException` を継承しない失敗がある。`InvalidRequestDescriptorException`、`IllegalStateException("Fanbox is closed")`、ダウンロード時の `IllegalArgumentException`
- 401 は endpoint を問わず `Unauthorized` になる

セッション管理側の事実は次のとおりである。

- `PixiViewViewModel`（`shared/src/commonMain/kotlin/me/matsumo/fanbox/PixiViewViewModel.kt:115-120`）が 10 分周期で疎通を確認し、成否で `_isLoggedInFlow` を決める
- `FanboxRepository.logoutTrigger`（`FanboxRepository.kt:60, 232, 237, 271`）が `Channel.CONFLATED` で `logout()` 完了を通知し、`PixiViewViewModel:102-106` が購読して `_isLoggedInFlow.emit(false)` する
- `PixiViewScreen`（`shared/src/commonMain/kotlin/me/matsumo/fanbox/components/PixiViewScreen.kt:58-71`）が `isLoggedIn` を見て `AnimatedContent` で welcome フローへ全画面差し替えする

「ログイン済み状態を解除して welcome へ差し替える」機構は既に存在するため、同型の通知を 1 本足すだけで再ログイン導線が成立する。

## Goals / Non-Goals

**Goals:**

- `FanboxException` の subtype を UI が扱える分類へ変換し、分類ごとに文言を切り替える
- 401 検知時にログイン済み状態を解除し、10 分のポーリングを待たずに再ログイン画面へ導く
- 分類を通さない既存のエラー生成箇所（課金など FANBOX 由来でないもの）を壊さない

**Non-Goals:**

- 429 の自動リトライやバックオフ、待機時間の表示
- Paging のエラー表示。`LoadState.Error` は `ScreenState` を通らない
- `suspendRunCatching` のシグネチャ変更
- スキーマ不一致の観測。先行 change `adopt-tolerant-list-callbacks` の範囲

## Decisions

### 分類は `core:model` に置き、判定可能な部分だけを純粋関数に切り出す

**決定**: `core/model/src/commonMain/kotlin/me/matsumo/fanbox/core/model/FanboxErrorKind.kt` に enum と 2 つの関数を置く。

```kotlin
/** FANBOX API の失敗を UI が扱える粒度へ分類した種別 */
enum class FanboxErrorKind {
    Unauthorized,
    Forbidden,
    NotFound,
    RateLimited,
    ServerError,
    Network,
    SchemaMismatch,
    Unknown,
}

/** HTTP ステータスから分類を決める。ステータスを伴わない失敗は null を返す */
internal fun fanboxErrorKindOf(statusCode: Int?): FanboxErrorKind?

/** FANBOX API の失敗を分類する。中断は呼び出し側で除外済みであることを前提とする */
fun Throwable.toFanboxErrorKind(): FanboxErrorKind
```

`toFanboxErrorKind()` は次の形になる。

```kotlin
fun Throwable.toFanboxErrorKind(): FanboxErrorKind = when (this) {
    is FanboxException.SchemaMismatch -> FanboxErrorKind.SchemaMismatch
    is FanboxException.Network -> FanboxErrorKind.Network
    is FanboxException -> fanboxErrorKindOf(statusCode) ?: FanboxErrorKind.Unknown
    else -> FanboxErrorKind.Unknown
}
```

HTTP ステータスを持つ 6 subtype は `statusCode` の判定で分類できるため、`fanboxErrorKindOf` に集約する。この関数は `Int?` を受ける純粋関数なので**単体テストできる**。

`SchemaMismatch` と `Network` は `statusCode` だけでは判別できない（両者とも null を取りうる）ため型分岐が残る。ここは 3 行の自明な分岐であり、テストは行わない。`FanboxException` の subtype はテストからインスタンスを生成できないため、これを網羅テストする手段は fankt の改修以外に無い。テストの網羅性を保証の中心に据えず、判定ロジックの側を純粋関数として担保する。

**代替案**: fankt に公開のテスト用 factory を追加する。却下した。fankt のリリースと version 引き上げが先行条件になり、この change のスコープを超える。

### `ScreenState.Error` は変更しない

**決定**: `ScreenState.Error` にプロパティを追加しない。

分類の結果は文言の選択に使われるだけであり、その選択はファクトリ側で完結する。`ErrorView` は従来どおり `message` と `retryTitle` を表示すれば要件を満たす。分類の値を `ScreenState.Error` に持たせても、現時点で読む箇所が無い。

`messageArgs` のようなフォーマット引数も追加しない。`RateLimited` の待機時間を文言へ埋め込むと、単位の丸め・複数形・ロケールごとの語順という問題が付いてくる。「しばらく待ってから再試行してください」という固定文言で利用者が取れる行動は変わらない。

結果として `core:model` の `ScreenState.kt` と `core:ui` の `ErrorView.kt` はいずれも無改修で済む。

### `ScreenState.Error` の生成を共通ファクトリに集約する

**決定**: `core:model` に拡張関数を置く。

```kotlin
/** FANBOX API の失敗を、分類に応じたエラー状態へ変換する */
fun Throwable.toScreenStateError(
    fallbackMessage: StringResource = Res.string.error_network,
    fallbackRetryTitle: StringResource? = null,
): ScreenState.Error
```

各 ViewModel は `onFailure = { it.toScreenStateError() }` と書く。分類ごとの文言の対応表はこの 1 箇所に集約する。

`fallbackRetryTitle` を持つのは `FanCardViewModel`（`feature/creator/src/commonMain/kotlin/me/matsumo/fanbox/feature/creator/fancard/FanCardViewModel.kt:49-53`）のためである。この画面は `message = creator_fan_card_not_supported` / `retryTitle = common_back` という固有の組み合わせを持ち、fallback 側で両方を保持できないと「戻る」が「再読み込み」に退行する。

対応表は次のとおりとする。

| 分類 | message | retryTitle |
|---|---|---|
| Unauthorized | `error_session_expired` | 既定 |
| Forbidden | `error_forbidden` | 既定 |
| NotFound | `error_no_data` | 既定 |
| RateLimited | `error_rate_limited` | 既定 |
| ServerError | `error_server` | 既定 |
| Network | `error_network_description` | 既定 |
| SchemaMismatch | `error_schema_mismatch` | 既定 |
| Unknown | `fallbackMessage` | `fallbackRetryTitle` |

`Unauthorized` は検知した時点で welcome へ差し替わるため、この文言が見えるのは差し替えが間に合わなかった一瞬に限られる。それでも定義するのは、差し替えの経路が塞がった場合に汎用文言へ落ちるのを避けるためである。retryTitle は既定（`common_reload`）のままとする。押しても各画面の再取得が走るだけであり、「ログイン」と表示して再取得だけが走るほうが誤解を生む。

### 文言の翻訳は既定の英語にフォールバックさせる

新規の 5 文言は `values/strings.xml` にのみ追加する。`values-ja` / `values-ko` / `values-ru` / `values-zh-rCN` には追加せず、Compose Resources の既定フォールバックで英語を表示させる。翻訳の追加はこの change の範囲外とし、必要になった時点で別途行う。

### 401 の通知は `logoutTrigger` と同じ機構を再利用する

**決定**: `FanboxRepository` に `sessionInvalidatedTrigger: Flow<Long>` を追加し、`_logoutTrigger` と同じく `Channel.CONFLATED` を使う。`PixiViewViewModel` の `init` に購読を追加して `_isLoggedInFlow.emit(false)` する。

`CONFLATED` はバッファを 1 件に保つが、collector が送信の間に処理すれば複数回受け取りうる。ただし `PixiViewScreen` の `AnimatedContent` は `isLoggedIn` の Boolean を見ており、同じ `false` を複数回受け取っても遷移は 1 度しか起きない。重複遷移の防止は `CONFLATED` ではなく state の性質によって成立する。

**代替案**: `logoutTrigger` に相乗りする。却下した。`logoutTrigger` は `logout()` の完了通知であり、Cookie 削除などの後始末が済んでいることを意味する。401 検知は別の事実である。

### 401 の検知は `FanboxRepositoryImpl` の private helper で行う

**決定**: fankt 呼び出しを包む private suspend ヘルパーを 1 つ置く。

```kotlin
private suspend fun <T> withSessionCheck(block: suspend () -> T): T {
    return try {
        block()
    } catch (e: FanboxException.Unauthorized) {
        _sessionInvalidatedTrigger.send(Random.nextLong())
        throw e
    }
}
```

例外は再スローするため、呼び出し側の `suspendRunCatching` は従来どおり `Result.failure` を受け取り、分類も従来どおり働く。

包む対象は fankt の HTTP を伴うメソッドで、実測でおよそ 32 箇所ある。`logout()` とセッション設定系は除外する。ログアウト処理中の 401 で通知すると `logoutTrigger` と二重に発火するためである。Pager 生成メソッドは HTTP を直接叩かず、PagingSource が `getHomePosts` などの委譲メソッドへ戻るため、基底のメソッドを包めば Paging 経由の 401 も検知できる。

適用漏れは `FanboxRepository.kt` 内の `fanbox.` 呼び出しを grep して確認する。漏れても従来どおり 10 分ポーリングで復帰するため退行はしない。

**代替案**: `suspendRunCatching` に検知を仕込む。却下した。`core:common` は fankt に依存せず、依存を足すとモジュール方向が歪む。

## Risks / Trade-offs

- **[分類の網羅テストができない]** `FanboxException` の subtype はテストから生成できない → `statusCode` からの判定を純粋関数に切り出してそこを網羅テストする。型分岐の 3 行は保証の外に置く。これは fankt を変えない限り解消しない制約であり、design に明記して受け入れる。
- **[401 の誤検知で作業が中断される]** FANBOX が一時的に 401 を返すと、利用者の操作なしに welcome 画面へ飛ぶ → 既存の 10 分ポーリングも同じ前提で `isLoggedIn` を落としている。検知が早まるだけであり、ネットワーク断で発火しない分だけ誤検知は少ない。
- **[`withSessionCheck` の適用漏れ]** 約 32 箇所を手で包むため漏れうる → grep で確認する。漏れても従来の挙動に戻るだけで退行しない。
- **[翻訳が英語のまま]** 新規 5 文言が日本語環境でも英語で出る → 分類が判明すること自体が現状からの改善であり、翻訳は後追いで足せる。
- **[Paging 経由では分類が効かない]** `PagingErrorSection` は `ScreenState` を通らない → 401 の検知と welcome への差し替えは repository 層にあるため Paging 経由でも働く。文言だけが従来のままになる。

## Migration Plan

永続化形式の変更もデータ移行も無い。`ScreenState.Error` の構造を変えないため、ViewModel の置き換えを 1 箇所ずつ段階的に進められる。

先行 change `adopt-tolerant-list-callbacks` が `FanboxRepository.kt` の list API 10 メソッドを書き換えているため、その後に着手する。両方を適用した最終形は次のようになる。

```kotlin
override suspend fun getSupportedPlans(): List<FanboxCreatorPlan> = withSessionCheck {
    fanbox.getSupportedPlans(::recordSchemaMismatch)
}
```

切り戻しは revert で足りる。

## Open Questions

なし。
