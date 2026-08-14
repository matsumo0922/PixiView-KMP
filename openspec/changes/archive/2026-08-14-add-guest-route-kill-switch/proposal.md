# 配信済み guest 経路の停止スイッチを Remote Config に置く

## Why

Android は投稿詳細の解析を配信済みの guest bundle で実行する（`fanbox-guest-route`）。遠隔から取得したコードを実行する以上、アプリの更新を待たずに実行そのものを止める手段が要る。

現状の停止手段は配信元（fankt 側の GitHub Pages）の操作だけである。これで足りない場合が 3 つ残る。

1. **manifest の取得が返らない**。`ZiplineGuestLoader` は manifest の取得に明示的なタイムアウトを設定しておらず、待ち時間はプラットフォームの既定値に従う（`fanbox-guest-route` の既存 spec に記載済み）。配信先が応答を返さない状態では manifest を 404 にしても取得の試行そのものは起きるため、配信元の操作では止められない。止めるには取得を試みないことが要る
2. **配信元を操作できない**。ホスティングの障害、権限の喪失、誤設定の巻き戻しに時間を要する状況では、配信元は制御面として使えない
3. **guest を止める判断が配信物と無関係に降りてくる**。ストアや規約の要請で遠隔コード実行そのものを直ちに停止する場合、配信物を差し替えても要請は満たせない

Firebase Remote Config は PixiView の Android に既に導入済みの Firebase 上で動き、配信元とは独立した制御面になる。上の 3 つはいずれも「アプリ側が guest を起動しないことを選ぶ」ことで閉じる。

### issue #139 の前提の訂正

issue には「暫定手段は manifest へ到達できる端末にのみ届く。到達できない端末を含めて確実に停止するには本 issue の対応が要る」とある。`ZiplineGuestLoader` は `ZiplineLoader` に `withCache` を渡しておらず、bundle のローカルキャッシュは存在しない。したがって manifest へ到達できない端末は guest を起動できず、既に直接経路で動作している。停止スイッチが埋めるのは「キャッシュ済みの壊れた bundle」ではなく、上の 3 つである。

## What Changes

- Android の `createFanbox` が Remote Config の停止フラグを参照し、立っている場合は配信先と公開鍵を渡さずに `Fanbox` を生成する
- 同じ関数が Remote Config の取得を背景で走らせ、次回のアプリ起動に反映させる
- 停止スイッチの存在、既定値、反映までの遅延を README に記載する

## 受け入れ条件との対応

issue #139 の受け入れ条件は、#143 による Android 限定化を受けて issue のコメントで改訂されている。改訂後の対応は次のとおり。

| 受け入れ条件 | 対応 |
| --- | --- |
| Android で Remote Config を参照し guest 経路の有効・無効を決める | 本 change |
| `FanboxRepositoryImpl` の `Fanbox` 生成へ配線する | 本 change |
| fetch 前および fetch 失敗時の既定値を決める | 本 change（design.md に記載し README へ反映） |
| リリースビルドが通る | 本 change |
| iOS へ Firebase SDK を導入する（改訂前の条件） | non-goal。iOS は guest を起動しないため停止スイッチの対象にならない |

issue の title は改訂前の「Firebase を iOS に導入し」のままである。本 change の範囲は Android に限る。

## Impact

- Affected specs: `fanbox-guest-route`
- Affected code: `core/repository/src/androidMain/kotlin/me/matsumo/fanbox/core/repository/FanboxFactory.android.kt`, `README.md`
- 依存の追加なし。`firebase-config` は `libs.bundles.firebase` に含まれ、`core:common` の `androidMain` が `api` で公開しているため `core:repository` の `androidMain` から参照できる
- iOS への影響なし。`FanboxFactory.ios.kt` は変更しない

## Non-goals

- iOS への Firebase SDK 導入（#139 の改訂で範囲外へ移動）
- 同一セッション内での即時反映。反映は次回のアプリ起動時とする
- 同梱 fallback bundle（#140）
- 停止スイッチ以外の Remote Config 利用
