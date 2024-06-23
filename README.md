
<h1 align="center">PixiView</h1>

<p align="center">
Enjoy a comfortable FANBOX life with PixiView!<br>
PixiView is an unofficial Android & iOS client app for pixivFANBOX.
</p>

<div align="center">
  <a href="./LICENSE">
    <img alt="LICENSE" src="https://img.shields.io/badge/license-GPL-blue.svg?maxAge=43200" />
  </a>
  <a href="">
    <img src="https://img.shields.io/badge/supports-Android%208+-AD2A5A">
  </a>
  <a href="https://open.vscode.dev/matsumo0922/PixiView-KMP">
    <img alt="Open in VSCode" src="https://img.shields.io/static/v1?logo=visualstudiocode&label=&message=Open%20in%20Visual%20Studio%20Code&labelColor=2c2c32&color=007acc&logoColor=007acc" />
  </a>
</div>

<hr>

<p align="center">
    <img src="media/google_play/screenshots.png" width="2048" alt="screenshots">
</p>

<p align="center">Do you speak Japanese? Japanese <a href="./README-JA.md">READEME</a> is Here!</p>

## Status
#### Ready!! :rocket:

Now available on GooglePlay and AppStore! Please download from the link below.
Or, if you're an mobile developer, you can build the app and install it yourself.
Contributions are always welcome. Try building your app by following the section below.

#### [Download](https://apps.apple.com/jp/app/fanviewer/id6476085758) from AppStore
#### [Download](https://play.google.com/store/apps/details?id=caios.android.fanbox) from GooglePlay
#### [Download](https://github.com/matsumo0922/PixiView-KMP/releases) from GitHub

## Why?

There was only a web version of FANBOX, which was a bit inconvenient for viewing posts on smartphones. By developing natively for smartphones, we have been able to include several convenient features such as the ability to download posts and receive new notifications.

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
  - Display posts from followed creators in chronological order.
  - Display posts from supported creators in chronological order.
  - Display followed creators.
  - Display supported creators.
  - Follow/Unfollow creators.
  - Like posts.
  - Search for creators.
  - Search by tags.
  - Search plans.
  - Display notifications.
  - Display messages.
- Download
  - Download in image/file/GIF format.
  - Download all images included in a post.
  - Download fan cards.
  - Bulk download function for individual creators.
- Ads
  - AdMob Native Ads

#### Not Ready...

- Pixiv integration feature.
- Widget functionality.

## Architecture
Shows the architecture diagram of the app. It's quite complex, so I've omitted some modules and dependencies to give you an overview.

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

This app uses Gradle's Convention Plugins to standardize the build logic, and all the logic is written in a module called `build-logic`. For information on this approach, see [nowinandroid](https://github.com/matsumo0922/nowinandroid/tree/main/build-logic).

If you find a bug, want to improve a feature, or want to develop a new feature, please first write an issue. Then assign yourself and work on the development. Pull requests are always welcome :smile:

This app is monetized using AdMob. When building manually from GitHub, you need to write the AdMob App ID in `local.properties`. By default it contains a dummy ID, which causes it to crash on startup. Alternatively, please delete the AdMob code and build the app. In addition, various IDs are described in `local.properties`. See `app/build.gradle.kts` or `PixiViewConfig` for details.

## License

```text
PixiView
Copyright (C) 2023 daichi-matsumoto

This program is free software: you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation, either version 3 of the License, or
(at your option) any later version.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program.  If not, see <https://www.gnu.org/licenses/>.

Also you can contact me by electronic mail at caios.system@gmail.com.

If the program does terminal interaction, make it output a short
notice like this when it starts in an interactive mode:

    PixiView  Copyright (C) 2023 daichi-matsumoto
    This program comes with ABSOLUTELY NO WARRANTY; for details type `show w'.
    This is free software, and you are welcome to redistribute it
    under certain conditions; type `show c' for details.

The hypothetical commands `show w' and `show c' should show the appropriate
parts of the General Public License.  Of course, your program's commands
might be different; for a GUI interface, you would use an "about box".

You should also get your employer (if you work as a programmer) or school,
if any, to sign a "copyright disclaimer" for the program, if necessary.
For more information on this, and how to apply and follow the GNU GPL, see
<https://www.gnu.org/licenses/>.

The GNU General Public License does not permit incorporating your program
into proprietary programs.  If your program is a subroutine library, you
may consider it more useful to permit linking proprietary applications with
the library.  If this is what you want to do, use the GNU Lesser General
Public License instead of this License.  But first, please read
<https://www.gnu.org/licenses/why-not-lgpl.html>.
```
