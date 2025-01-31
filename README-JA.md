<h1 align="center">PixiView</h1>

<p align="center">
PixiView で快適な FANBOX ライフを！<br>
PixiView は PIXIV FANBOX の非公式 Android & iOS クライアントアプリです
</p>

<div align="center">
  <a href="./LICENSE">
    <img alt="LICENSE" src="https://img.shields.io/badge/license-GPL-blue.svg?maxAge=43200" />
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
    app  
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
  app --> library
  app --> creator
  app --> post
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

## Contribute

このアプリは Gradle の Convention Plugins を用いてビルドのロジックを共通化しており、`build-logic` というモジュールに全てのロジックが記述されています。このアプローチに関しては、[nowinandroid](https://github.com/matsumo0922/nowinandroid/tree/main/build-logic) をご覧ください。

何か不具合を発見したり機能を改善したい場合、機能を新たに開発したい場合は、まず issue を書いてください。その上であなた自身を assign し、開発に取り組んでください。pull request はいつでも歓迎です :smile:

このアプリは AdMob を用いて収益化しています。GitHub から手動でビルドする際は AdMob App ID を `local.properties` に記述する必要があります。デフォルトではダミーの ID が入っているため、起動時にクラッシュします。もしくは AdMob の当該コードを削除してからアプリをビルドしてください。その他、 `local.properties` には様々な ID が記述されています。詳細は `app/build.gradle.kts` または `PixiViewConfig` をご覧ください。

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
