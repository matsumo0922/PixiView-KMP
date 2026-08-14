# guest 経路の有効化を Android 限定にする

## Why

`FanboxRepositoryImpl` は `commonMain` にあり、プラットフォームによる分岐を持たない。このため現在は Android と iOS の双方で配信済み guest が実行される。

OTA 配信の適用範囲を当面 Android に限る方針となった。この方針のもとで kill switch（#139）を Android 限定に実装すると、iOS には遠隔コードを実行しながらアプリ側から停止する手段が無い状態が残る。停止手段を持たないプラットフォームで遠隔コードを実行し続ける理由がないため、iOS を直接経路へ戻す。

## What Changes

- `Fanbox` の生成をプラットフォーム側へ委ね、Android のみが配信先と公開鍵を渡す
- 配信先の manifest URL、鍵名、Ed25519 公開鍵を Android の source set へ移す
- iOS は guest を伴わないコンストラクタで `Fanbox` を生成する

`core/repository` は `androidMain` と `iosMain` の source set を既に持ち、`repositorySubModule` で `expect` / `actual` を使っている。実行時のフラグではなくこの仕組みで表現する。

破壊的変更はない。iOS の挙動は #138 以前と同じになる。

## Capabilities

### New Capabilities

なし。

### Modified Capabilities

- `fanbox-guest-route`: guest を使う対象を Android に限る。iOS では guest を起動せず直接経路で処理することを定める

## Impact

### 変更するコード

- `core/repository/src/commonMain/kotlin/me/matsumo/fanbox/core/repository/FanboxRepository.kt`
- `core/repository/src/androidMain/kotlin/me/matsumo/fanbox/core/repository/FanboxFactory.android.kt`（新規）
- `core/repository/src/iosMain/kotlin/me/matsumo/fanbox/core/repository/FanboxFactory.ios.kt`（新規）
- `README.md`

### 実行時の挙動

Android は変わらない。iOS は `post.info` の解析が直接経路へ戻り、配信済みの修正が届かなくなる。FANBOX の仕様変更が起きた場合、iOS はストア審査を経た更新でのみ修正が届く。

## 対象範囲

- guest 経路の対象を Android に限ること
- 配信先と鍵を Android の source set へ閉じること
- README の記載を実態に合わせること

## 非対象範囲

- Remote Config による kill switch（#139）
- 同梱 fallback bundle（#140）
- iOS で guest を動かすための検討。方針が変わった時点で改めて扱う

## ドキュメント影響

あり。`README.md` の「Remote parsing updates」に対象が Android である旨を加える。
