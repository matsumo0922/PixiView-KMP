# PixiView Agent Notes

共通ルール（口調、Git/PR、RTK）は `~/.codex/AGENTS.md` と `~/.codex/RTK.md` に従うこと。このファイルには `pixiview` 固有の補足だけを書く。

## Worktree 運用

実装を行う場合は、必ず worktree を作成し、デフォルトディレクトリを汚さない。
特別に指示がある場合や、read-only の調査やビルド・テストの実行はこの限りでない。

```bash
git worktree add ../pixiview-<task-slug> -b <branch-name>
cp -p local.properties ../pixiview-<task-slug>/local.properties
cd ../pixiview-<task-slug>
```

- `local.properties` は git 管理外なので、`git worktree add` ではコピーされない。ビルドに必要な情報が記載されているため、worktree 作成直後に必ず元 checkout からコピーする。

<!-- agents-rules:kotlin:begin -->
<!-- この区間は Agents リポジトリが管理する。編集は Agents の rules/kotlin.md で行い、make link-project で更新する -->
# Kotlin / Jetpack Compose 規約

Kotlin プロジェクト共通のコーディング規約。整形など静的解析で判定できる規約（trailing comma、メソッドチェーンの改行、Composable の `modifier` 引数、ラムダ引数名の時制、Immutable なコレクション型の強制など）は `rules/lint/` のテンプレートを取り込んだ detekt / compose-rules 設定で強制する。このファイルには静的解析で判定できない、判断を伴う規約だけを置く。

## Kotlin

### 命名

- 変数名は役割が分かる名前にする。ループの添字も `i` ではなく `index` とする
- KDoc は日本語で書く。対象は定数値 / `data class` / `enum` / `data object` / `object` / `class`（Activity / Fragment / Dialog / ViewModel を除く）

### 型参照と import

- コード本体では完全修飾名を使わず、対象を import して参照する
- 通常のクラスは型名だけで参照する。`Hoge.Fuga` のように所有元を付けず、`Fuga` を import して `Fuga` と書く
- `enum` の定数と `sealed class` / `sealed interface` の subtype は、型名を含む `Hoge.Fuga` まで許可する
- `enum` または sealed hierarchy でも `Hoge.Fuga.Piyo` のような3段以上の参照は禁止する。先頭側を import で省略し、`Fuga.Piyo` と書く

### 標準 API と extension

- platform API を直接呼ぶ前に、対応する ktx extension があるか確認して使う
- `Bitmap.createBitmap(w, h, config)` ではなく `androidx.core.graphics.createBitmap(w, h)` を使う
- `canvas.save()` / `restore()` の対で囲む処理は `withScale` / `withTranslation` / `withRotation` / `withClip` / `withMatrix` を使う。状態を戻し損ねる種類のバグを scope で防げる
- ミリ秒の `Long` を渡す API では `kotlin.time.Duration` を使う（`300L` ではなく `300.milliseconds`）。単位が型に載るため取り違えが起きない

### 分割

- 同じ処理を2箇所目に書こうとした時点で、共通メソッドまたは共通クラスへ切り出す
- ラムダ本体は3文以内に収める。4文以上、または分岐を2つ以上含む場合はメソッドへ切り出す
- 論理演算子（`&&` / `||`）を2つ以上含む条件式は、意味を表す名前の Boolean 変数に切り出してから比較に使う

### 定数

- 参照箇所が1〜2箇所しかない値は定数へ切り出さず、使用箇所へ直接書く。比率、角度、書式文字列、log tag のいずれも対象とする
- 参照箇所は「宣言を除いた、コード本体からの参照」で数える。KDoc の `[Xxx]` 参照は数に含めない
- 定数名は参照箇所から値を隠す。`side * BadgeSizeRatio` は宣言まで辿らないと `0.3f` だと分からず、読む手間だけが増える
- 値に理由が要る場合は、定数の KDoc ではなく使用箇所のコメントとして書く
- 次のものは参照が1箇所でも定数のまま残す。インライン化すると意味が変わる
    - 複数の分岐で比較対象になる sentinel（`if (text == UnavailableText)` のような値）。リテラルを散らすと、値を変えたときに片方だけ直す事故になる
    - `remember` の key として同一性が比較される値。呼び出しごとに新しい instance を作ると key が毎回変わり、再計算が走る
    - `Paint` のように、生成を避けて process 内で共有している instance

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
- 関数呼び出しは引数2つまでなら名前付き引数なしで1行で書く。次のいずれかに該当する場合は、引数ごとに改行して名前付き引数を使う: 引数が3つ以上 / 同じ型・形の引数が連続する /
  引数内にメソッドチェーン・匿名オブジェクト・計算式・関数呼び出しが入る
- 行の長さは基準にしない。`Text(stringResource(R.string.ok))` は短くても、引数内で関数を呼んでいるため改行する
- Java メソッドは名前付き引数を使えないため、maxLength 制限に当たらない限り、改行せず1行で呼び出す

### テスト

- テストは production の関数を呼ぶ。同じ算出をテスト側へ書き写さない。複製したテストは production が壊れても通る
- 検証したいロジックが platform 依存の処理へ埋まっている場合は、純粋関数として切り出してからテストする
- 境界値は「等しい」で代用しない。「過ぎている」を検証するなら、同値ではなく実際に過ぎた入力を渡す
- 修正に対するテストは、修正を戻すと落ちることを一度確認する

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

- Composable を定義する際は必ず引数に Modifier を定義し、デフォルト引数として `Modifier` を渡すこと。なお、デフォルト引数を持つ引数が複数ある場合はその中で一番最初に定義すること
- `@Preview` を付けた Composable は対象外とする。呼び出し元が preview host だけであり、Modifier を渡す経路がない
- Dialog も対象とする。material3 の Dialog は Modifier を受け取るため、渡す先がある
- 内部で Modifier のチェインを組み立てる場合は、受け取った Modifier を起点にする。`Modifier.size(x)` と書くと引数が無視され、外部からの指定が効かない
- size やマージンとして使われる padding などは外部から Modifier を通じて指定するべきであり、特段の事情がない限り内部でサイズや padding を固定化しない
- Composable 自体を if で描画しない時などは内部で `if (!isVisible) return` のように早期リターンするのではなく、その Composable の呼び出し下で分岐する
- 特に指示がなくかつ利用可能な場合は、material3 component を積極的に用い、material2 を混ぜない
- `Column` / `Row` の子要素間の間隔は `Arrangement.spacedBy` と各要素の `Modifier.padding` で調整する。固定サイズの `Spacer`（`Spacer(Modifier.width(8.dp))` など）は間隔調整に使わない
- `Spacer(Modifier.weight(1f))` のような比率ベースの `Spacer` は使ってよい
- `dp` や `sp`、比率などのレイアウト固有の数値は別途定数には切り出さず、値を直接使う

```kotlin
@Composable
private fun Badge(
    side: Dp,
    modifier: Modifier = Modifier,
) {
    // 起点を modifier にする。Modifier.size(side) と書くと引数が捨てられる
    Box(modifier = modifier.size(side))
}
```

### 呼び出し

- Composable の呼び出しは名前付き引数を使い、引数ごとに必ず改行する。
- 例外は、引数が1つでその値が識別子1つの場合（`IconButton(onDelete)`）。名前付きにも改行にもしない
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
