## Context

`FanboxRepositoryImpl`（`core/repository/src/commonMain/kotlin/me/matsumo/fanbox/core/repository/FanboxRepository.kt:199-598`）は fankt の `Fanbox` インスタンスを内部に持ち、各 API をほぼ 1:1 で委譲する薄いラッパーである。例外は捕捉せず呼び出し側へ素通しし、ViewModel と PagingSource が `suspendRunCatching` で受け止める。

fankt 0.1.1 の list API は callback 無し / callback 付きの 2 つの overload を持つ。実装を読むと、両者の関係は次の 2 通りに分かれる。

- 9 系統（`getHomePosts` / `getSupportedPosts` / `getCreatorPosts` / `getPostComment` / `getFollowingCreators` / `getFollowingPixivCreators` / `getRecommendedCreators` / `getCreatorPlans` / `getBells`）は、どちらも同じ tolerant なリポジトリ経路を通る。callback 無しの版は `FanboxTolerantResult.value` を取り出して mismatches を捨てているだけで、**返る項目は完全に同一**である。callback 付きへの移行は観測の追加にすぎず、挙動を変えない。
- `getSupportedPlans` だけは overload ごとに別のデシリアライザを使う。callback 無しは strict なエンティティを通し、`init` の `require` で `userId` が Long に変換できない項目を弾いて `IllegalArgumentException` を投げる。これが fankt 内部で `FanboxException.SchemaMismatch` に変換され、呼び出し全体が失敗する。callback 付きは tolerant なエンティティを使い、壊れた項目を落として残りを返す。**この 1 本だけが挙動の変わる移行である。**

callback は `(FanboxListItemSchemaMismatch) -> Unit` という非 suspend の関数型で、呼び出し側のコルーチンコンテキストで実行され、callback が投げた例外は呼び出し元へ伝播する。`FanboxListItemSchemaMismatch` は `endpoint: String` と `indexPath: List<Int>` のみを持つ data class で、応答本文も認証情報も含まない。

一方 fankt 自身は、`logLevel != FanboxLogLevel.NONE` のときに `includeRawFragment = true` として（`Fanbox.kt:139`）、スキーマ不一致ごとに item の JSON を Napier へ出力する（`FanboxListItemSchemaMismatch.kt:58-71`）。PixiView は現在 `FanboxLogLevel.INFO` を指定している（`FanboxRepository.kt:217`）。credential key は redact されるが、FANBOX と利用者のデータは残る。

## Goals / Non-Goals

**Goals:**

- `getSupportedPlans()` を callback 付きへ移行し、1 件のスキーマ不一致で支援プラン一覧が全体失敗する状態を解消する
- callback overload を持つ list API 10 メソッドで、スキップされた項目を Crashlytics に残す
- リリースビルドで fankt が item の JSON を出力しないようにする
- `FanboxRepository` の interface シグネチャと呼び出し側（ViewModel / PagingSource / UI）を一切変更しない

**Non-Goals:**

- `FanboxException` の subtype に応じたエラー表示、再ログイン導線。後続 change `classify-fanbox-errors` の範囲
- callback overload が存在しない strict なエンドポイントへの対処。fankt 0.1.1 に緩和手段が無い
- スキーマ不一致の発生率を Firebase Analytics で集計する仕組み
- スキップされた事実の UI 表示

## Decisions

### 記録は `FanboxRepositoryImpl` の private メソッド 1 つで済ませる

**決定**: 専用のインターフェースやクラスを作らず、`FanboxRepositoryImpl` に private メソッドを 1 つ置く。

```kotlin
/**
 * fankt の list 応答でスキップされた項目をクラッシュレポート基盤へ記録する。
 *
 * [FanboxListItemSchemaMismatch] は endpoint と indexPath のみを持つため、
 * 送信内容に FANBOX や利用者のデータは含まれない。
 * ログ出力は fankt が同じ内容を Napier へ出すため行わない。
 */
private fun recordSchemaMismatch(mismatch: FanboxListItemSchemaMismatch) {
    recordException(
        IllegalStateException(
            "FANBOX list item schema mismatch. endpoint=${mismatch.endpoint}, indexPath=${mismatch.indexPath}",
        ),
    )
}
```

各 list API はこれをメソッド参照で渡す。

```kotlin
override suspend fun getSupportedPlans(): List<FanboxCreatorPlan> {
    return fanbox.getSupportedPlans(::recordSchemaMismatch)
}
```

Napier への出力を行わないのは、fankt が `logLevel` の値によらず mismatch ごとに endpoint と indexPath を Napier へ出しているためである（`FanboxDiagnostics.kt:73-76`）。PixiView が同じ内容をもう 1 行出すと、1 件のスキップにつき 2 行がログに並ぶだけになる。観測先として確定している「Napier ログ + Crashlytics」のうち、Napier は fankt の既存出力が満たす。

**代替案**: 差し替え可能な `FanboxSchemaMismatchReporter` インターフェースを `core:common` に新設する。却下した。呼び出し元は `FanboxRepositoryImpl` の 10 メソッドだけであり、差し替えの必要も他モジュールからの利用も無い。

**代替案**: 専用の例外型 `FanboxSchemaMismatchException` を定義する。却下した。message に endpoint が入るため、型を分けなくても各イベントから endpoint を読める。Crashlytics のグルーピングは例外の型とスタックフレームに強く影響されるため、同じ呼び出し箇所からの記録が 1 つの issue にまとまる可能性はあるが、観測の目的は満たす。

**代替案**: `runCatching` で記録を包み、記録の失敗が API 呼び出しを壊さないようにする。却下した。`recordException` は Android actual が Firebase の呼び出し、iOS actual は no-op であり、例外を投げない。起こらない失敗に備えるコードは書かない。

### `logLevel` をビルドで分ける

**決定**: `PixiViewConfig` に `isDebug: Boolean` を追加し、`FanboxRepositoryImpl` が `Fanbox` を生成するときの `logLevel` を切り替える。

```kotlin
private val fanbox = Fanbox(
    logLevel = if (pixiViewConfig.isDebug) FanboxLogLevel.INFO else FanboxLogLevel.NONE,
    ioDispatcher = ioDispatcher,
    cookieStorage = cookieStorage,
)
```

`isDebug` の供給は `AppModule.android.kt` / `AppModule.ios.kt` で行う。`PixiViewConfig` は既に `core:common` にあり、`core:repository` は `core:common` に依存しているため、新しい依存は増えない。

**Android は `ApplicationInfo.FLAG_DEBUGGABLE` を実行時に読む。**

```kotlin
val context: Context = androidContext()
val isDebug = (context.applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE) != 0
```

`BuildConfig.DEBUG` は使えない。`shared` は `com.android.kotlin.multiplatform.library` の `KotlinMultiplatformAndroidLibraryTarget` を単一 target として持ち（`build-logic/src/main/java/primitive/KmpAndroidPlugin.kt:13-27`）、build type の概念も `BuildConfig` の生成も無い。`androidApp` の `BuildConfig` は app → shared の依存方向のため `shared` からは参照できない。

`BuildKonfig` に Boolean を 1 つ足す案も却下する。`BuildKonfig` の `defaultConfigs` は build type を区別しないため、release で `false` になる保証を作れない。設定を誤ると release で `INFO` になり、利用者のデータがログに出る。gate が fail-safe にならない方式は採らない。

`androidContext()` は `PixiViewApplication`（`androidApp/src/main/kotlin/me/matsumo/fanbox/PixiViewApplication.kt:42`）で既に Koin へ登録されている。`FLAG_DEBUGGABLE` は APK の実際の debuggable 属性を反映するため、release ビルドで `true` になることはない。

**iOS は `Platform.isDebugBinary` を用いる。** `kotlin.experimental.ExperimentalNativeApi` の opt-in が必要なため、`AppModule.ios.kt` に `@OptIn(ExperimentalNativeApi::class)` を付ける。

これによりリリースビルドでは fankt の item JSON 出力が止まり、PixiView 側の `recordSchemaMismatch`（endpoint と indexPath のみ）が Crashlytics 観測の正本になる。デバッグビルドでは fankt の詳細ログも残るため、スキーマがどう変わったかを開発時に読める。

**代替案**: 常に `NONE` にする。却下した。fankt の HTTP ログも一緒に消え、開発時の切り分けが難しくなる。

**代替案**: `INFO` のまま spec を実態に合わせる。却下した。リリースビルドで利用者のデータがログに出続けるのは、この change が `getSupportedPlans` を tolerant に変えることで新たな出力経路を 1 つ増やす以上、放置すべきでない。

### `getBells` は 3 引数 overload を使う

fankt の `getBells` は callback が `markNotificationsRead` より前に来る。PixiView は既読化を維持する必要があるため、名前付き引数で 3 引数の版を呼ぶ。

```kotlin
return fanbox.getBells(
    page = page,
    onItemSchemaMismatch = ::recordSchemaMismatch,
    markNotificationsRead = true,
)
```

### `getSupportedPlans` の挙動変化は UI に出さない

支援プラン一覧は課金情報であり、「一部を表示できませんでした」という表示は利用者に不要な不安を与える。現状は全体エラーで 1 件も見えないため、デコードできた分を黙って表示するのは厳密に改善である。スキップの事実は Crashlytics に残るため、開発側は把握できる。

## Risks / Trade-offs

- **[Crashlytics のノイズ増]** スキーマ不一致が恒常的に起きているエンドポイントがあると、非致命的例外が大量に送られる → 発生量はリリース後に確認する。多発が判明した時点で対処する。事前に上限や集約の仕組みを入れることはしない。
- **[`getSupportedPlans` の部分表示が誤解を生む]** 実際には支援しているプランが一覧から欠ける → 現状は全体エラーで 1 件も表示されないため、部分表示は退行しない。
- **[iOS では Crashlytics 記録が無い]** `recordException` の iOS actual は no-op である → Napier のログは iOS でも出る。本番の集計が Android のみになるのは既存の制約であり、この change で変えない。spec もこの実態に合わせて記述する。
- **[strict なエンドポイントは無防備なまま]** `getPostDetail` などは 1 件の不一致で失敗し続ける → fankt 側に緩和手段が無いため対処できない。この change では扱わない。

## Migration Plan

データ移行も永続化形式の変更も無い。fankt のバージョンも変えない。呼び出し側の変更が無いため、`FanboxRepositoryImpl` と `PixiViewConfig` の変更だけで完結する。

切り戻しは revert で足りる。

## Open Questions

なし。
