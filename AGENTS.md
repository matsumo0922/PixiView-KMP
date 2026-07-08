<!-- agents-rules:kotlin:begin -->
<!-- この区間は Agents リポジトリが管理する。編集は Agents の rules/kotlin.md で行い、make link-project で更新する -->
# Kotlin / Jetpack Compose 規約

Kotlin プロジェクト共通のコーディング規約。整形など静的解析で判定できる規約（trailing comma、メソッドチェーンの改行、Composable の `modifier` 引数、ラムダ引数名の時制、Immutable なコレクション型の強制など）は `rules/lint/` のテンプレートを取り込んだ detekt / compose-rules 設定で強制する。このファイルには静的解析で判定できない、判断を伴う規約だけを置く。

## Kotlin

### 命名

- 変数名は役割が分かる名前にする。ループの添字も `i` ではなく `index` とする
- KDoc は日本語で書く。対象は定数値 / `data class` / `enum` / `data object` / `object` / `class`（Activity / Fragment / Dialog / ViewModel を除く）

### 分割

- 同じ処理を2箇所目に書こうとした時点で、共通メソッドまたは共通クラスへ切り出す
- ラムダ本体は3文以内に収める。4文以上、または分岐を2つ以上含む場合はメソッドへ切り出す
- 論理演算子（`&&` / `||`）を2つ以上含む条件式は、意味を表す名前の Boolean 変数に切り出してから比較に使う

### 段落

- 処理は意味のまとまり（値の算出 / 条件分岐 / 早期 return / 副作用を伴う呼び出し / 最終 return）ごとに空行で区切り、段落として読めるようにする

```kotlin
/** 商品を購入し、レシートを発行する */
fun purchase(item: Item): Result<Receipt> {
    val totalPrice = item.price * item.quantity

    if (totalPrice <= 0) return Result.failure(InvalidPriceException(item))

    val receipt = paymentClient.charge(totalPrice)

    return Result.success(receipt)
}
```

### エラー処理

- 失敗が呼び出し側の処理対象になる場合は `Result`（または `runCatching`）を返す
- `throw` は事前条件違反だけに使い、`require` / `requireNotNull` / `error` で表現する
- catch した例外は握りつぶさず、ログに出力するか `Result.failure` に変換する

### 引数

- 関数定義は引数2つまでなら1行で書く。デフォルト引数を含む場合は引数ごとに改行する
- 関数呼び出しは引数2つまでなら名前付き引数なしで1行で書く。次のいずれかに該当する場合は、引数ごとに改行して名前付き引数を使う: 引数が3つ以上 / 同じ型・形の引数が連続する / 引数内にメソッドチェーン・匿名オブジェクト・計算式が入る
- Java メソッドは名前付き引数を使えないため、1行が長くなる場合は改行だけ行う

## Jetpack Compose

### 状態と安定性

- Composable から参照する `data class` には `@Immutable`（全プロパティが不変の場合）または `@Stable`（可変プロパティを含む場合）を付ける。Compose コンパイラの安定性推論に頼らず宣言することで、recomposition のスキップを保証するため

```kotlin
/** レシピ一覧画面の UI 状態 */
@Immutable
data class RecipeListUiState(
    val recipes: ImmutableList<Recipe>,
    val isLoading: Boolean,
)
```

### 命名

- private でない Composable には、配置先の画面 Composable の名前を prefix として付ける。複数の画面から参照される Composable には prefix を付けない

```kotlin
// HomeScreen に配置する Composable
@Composable
internal fun HomeTopAppBar(...)

// RecipeDetailScreen に配置する Composable
@Composable
internal fun RecipeDetailTopAppBar(...)
```

### 可視性

- Composable は `internal fun` で定義し、モジュール外から使う場合のみ `public` にする
- 1つの Composable からのみ呼ばれる分割済みの Composable は `private fun` にする

### レイアウト

- `Column` / `Row` の子要素間の間隔は `Arrangement.spacedBy` と各要素の `Modifier.padding` で調整する。固定サイズの `Spacer`（`Spacer(Modifier.width(8.dp))` など）は間隔調整に使わない
- `Spacer(Modifier.weight(1f))` のような比率ベースの `Spacer` は使ってよい

### 呼び出し

- Composable の呼び出しは名前付き引数を使い、引数ごとに改行する。引数が1つで短い場合のみ、1行・名前付き引数なしで書いてよい
- `modifier` 引数は呼び出し時の引数の先頭に置く
- `Modifier` のチェインが2リンク以上になる場合は、リンクごとに改行する

```kotlin
HomeTopAppBar(
    modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    title = uiState.title,
    onNavigationClick = { viewModel.close() },
)
```
<!-- agents-rules:kotlin:end -->
