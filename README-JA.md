<h1 align="center">PixiView</h1>

<p align="center">
PixiView で快適な FANBOX ライフを！<br>
PixiView は PIXIV FANBOX の非公式 Android & iOS クライアントアプリです
</p>

<div align="center">
  <a href="./LICENSE">
    <img alt="LICENSE" src="https://img.shields.io/badge/license-CC--BY--NC%204.0-lightgrey" />
  </a>
  <a href="">
    <img src="https://img.shields.io/badge/supports-Android%208+-AD2A5A">
  </a>
  <a href="https://open.vscode.dev/matsumo0922/PixiView">
    <img alt="Open in VSCode" src="https://img.shields.io/static/v1?logo=visualstudiocode&label=&message=Open%20in%20Visual%20Studio%20Code&labelColor=2c2c32&color=007acc&logoColor=007acc" />
  </a>
</div>

<hr>

<p align="center">
    <img src="media/google_play/screenshots.png" width="2048" alt="screenshots">
</p>


## Status
#### Ready!! :rocket:

GooglePlay または AppStore にて公開中です！下記のリンクからダウンロードしてください。
また、モバイルアプリ開発者の方はアプリをビルドして、自分でインストールすることもできます。
コントリビュートはいつでも歓迎です。下のセクションに従ってアプリをビルドしてみてください。

#### [Download](https://apps.apple.com/jp/app/fanviewer/id6476085758) from AppStore
#### [Download](https://play.google.com/store/apps/details?id=caios.android.fanbox) from GooglePlay
#### [Download](https://github.com/matsumo0922/PixiView-KMP/releases) from GitHub

## Why?

FANBOX には Web 版しかなく、スマートフォンで投稿を閲覧するには少し不便だったからです。スマートフォン向けアプリとしてネイティブで開発することによって、投稿のダウンロード機能や新着通知などの便利な機能を複数搭載できました。

## Tech Stack

- [Kotlin](https://kotlinlang.org/)
- [Kotlin Coroutines](https://kotlinlang.org/docs/coroutines-overview.html)
- [Kotlin Flow](https://kotlinlang.org/docs/flow.html)
- [Kotlin Multiplatform](https://www.jetbrains.com/ja-jp/kotlin-multiplatform/)
- [Jetpack Compose](https://developer.android.com/jetpack/compose?hl=ja)
- [Jetpack Compose Multiplatform](https://www.jetbrains.com/ja-jp/lp/compose-multiplatform/)
- [Material3](https://m3.material.io)

## Feature
#### Ready!!

- FANBOX
  - フォローしているクリエイターの投稿を時系列順に表示
  - 支援しているクリエイターの投稿を時系列順に表示
  - フォローしているクリエイターを表示
  - 支援しているクリエイターを表示
  - クリエイターのフォロー / フォロー解除
  - 投稿のいいね
  - クリエイター検索
  - タグ検索
  - プラン検索
  - 通知の表示
  - メッセージの表示
- ダウンロード
  - 画像 / ファイル / GIF 形式でダウンロード
  - 投稿に含まれるすべての画像をダウンロード
  - ファンカードのダウンロード
  - クリエイター単位での一括ダウンロード機能
- 広告
  - AdMob Native 広告

#### Not Ready...

- Pixiv 連携機能
- ウィジェット機能

## Architecture
アプリのアーキテクチャ図を示します。 だいぶ複雑になっているので、一部のモジュールや依存関係は省略し、概略を掴める形にしています。

```mermaid
%%{
init: {
'theme': 'neutral'
}
}%%

graph LR
  subgraph gradle
    build-logic
  end
  subgraph application
    androidApp
    iosApp
    shared
  end
  subgraph core
    common
    datastore
    model
    repository
    ui
  end
  subgraph feature
    library
    creator
    post
  end
  androidApp --> shared
  iosApp --> shared
  shared --> library
  shared --> creator
  shared --> post
  library --> ui
  library --> repository
  post --> ui
  post --> repository
  creator --> ui
  creator --> repository
  ui --> model
  repository --> datastore
  datastore --> model
  model --> common
```

## 静的解析

静的解析は detekt 2.0.0-alpha.6、ktlint wrapper、Compose rules 0.6.8 を使う。
root と `build-logic` の Kotlin ファイルおよび各モジュールの Kotlin ファイルを、次のコマンドで確認する。

```bash
./gradlew detekt reportMerge --continue
./gradlew :androidApp:lintDebug
```

自動整形には `./gradlew detekt --auto-correct --continue` を使い、続けて自動修正を付けないコマンドを実行する。
統合した Checkstyle レポートは `build/reports/detekt/merge.xml`、モジュールごとの HTML レポートは各モジュールの `build/reports/detekt/detekt.html` に出力する。
統合対象は通常の `detekt` の結果で、型解決タスクの結果は混ぜない。

関数定義とコンストラクタは、引数が3つ以上、またはデフォルト引数を持つ場合に引数ごとに改行する。
`FunctionSignature` / `ClassSignature` は引数3つ以上の定義を検査する。
デフォルト引数による改行、呼び出し側の名前付き引数と改行、Modifier の先頭配置、Composable の PascalCase と画面名の prefix は手動レビューの対象となる。
メソッドチェーンはプロパティ参照を数えず、1行2呼び出しまでとする。
`MaxChainedCallsOnSameLine` はプロパティ参照も数えるため無効化し、チェーンと Modifier のリンクごとの改行は手動で確認する。
行長は折り返しの基準にしない。

`config/detekt/detekt.yml` は Agents の共通テンプレートと同じ設定を使う。
複雑度や関数数の閾値は detekt の既定値を使い、関数数の上限は11となる。
責務を一緒に扱う理由がある宣言にだけ、直前の理由コメント付きで `@Suppress` を付ける。
初期カーソルの取得が必要な Flow factory、画面ごとのイベント窓口、宣言用 UI DSL、遷移・フォーカスなどの effect の実行タイミングも、この方法で例外を明示する。
未使用の fork の整理は [Pager/Snapper の移行](https://github.com/matsumo0922/PixiView-KMP/issues/163) で追跡する。
Swift 側の変更を伴う [iOS entry point の整理](https://github.com/matsumo0922/PixiView-KMP/issues/164) と、既存レイアウトを維持する [Modifier の二重適用の解消](https://github.com/matsumo0922/PixiView-KMP/issues/165) も個別に追跡する。
Composable も `CyclomaticComplexMethod` の対象とする。
`ModifierMissing` は全可視性を対象とし、Preview と自身のレイアウトを持たないラッパーを除外する。

full モードは Android の main / host test / app debug を対象とし、iosMain の型情報が必要なルールは対象に含めない。
iOS は light と実際のコンパイルで確認する。
型情報が必要なルールは、通常の `detekt` の統合レポートには含めず、次の full モードのタスクで検査する。

```bash
./gradlew detektMainAndroid detektHostTestAndroid :androidApp:detektDebug :androidApp:detektDebugUnitTest :androidApp:detektDebugAndroidTest --continue
```

各モジュールの `check` と CI からも対応する full モードのタスクを実行する。
これらのタスクは detekt が生成した source set の範囲を維持し、build ディレクトリ配下の生成物を除外する。
KMP の `expect` / `actual` に関するコンパイラ解析診断が出る場合は、ルールの指摘件数、解析診断、実際の Android/iOS のコンパイル結果を分けて確認する。
ログイベントを生成するテンプレートは `scripts/daifuku/templates/LogCategory.kt.erb` に置き、生成された宣言と合わせて更新する。
生成後は `./gradlew :core:logs:detekt --auto-correct` で整形を確認する。

## Contribute

このアプリは Gradle の Convention Plugins を用いてビルドのロジックを共通化しており、`build-logic` というモジュールに全てのロジックが記述されています。このアプローチに関しては、[nowinandroid](https://github.com/matsumo0922/nowinandroid/tree/main/build-logic) をご覧ください。

何か不具合を発見したり機能を改善したい場合、機能を新たに開発したい場合は、まず issue を書いてください。その上であなた自身を assign し、開発に取り組んでください。pull request はいつでも歓迎です :smile:

このアプリは AdMob を用いて収益化しています。GitHub から手動でビルドする際は AdMob App ID を `local.properties` に記述する必要があります。デフォルトではダミーの ID が入っているため、起動時にクラッシュします。もしくは AdMob の当該コードを削除してからアプリをビルドしてください。その他、 `local.properties` には様々な ID が記述されています。詳細は `androidApp/build.gradle.kts`、`shared/build.gradle.kts`、`core/common/src/commonMain/kotlin/me/matsumo/fanbox/core/common/PixiViewConfig.kt` をご覧ください。

## License

```text
PixiView
Copyright 2025 daichi-matsumoto

Licensed under the Creative Commons NonCommercial License (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

https://creativecommons.org/licenses/by-nc/4.0/

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
