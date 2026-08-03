# issue #109 — FANBOX セッションを secure store へ移行する

このファイルは PR description の下書きであり、PR 作成後に削除する。

## 目的

FANBOX の認証 Cookie の保存先を、平文の Room DB (`fankt.db`) から
プラットフォームの secure store（Android Keystore / iOS Keychain）へ移す。
既存ユーザーはアプリ更新後もログイン状態を維持する。

## 依存ライブラリ

`eu.anifantakis:ksafe:3.0.0`（Apache-2.0）を commonMain に追加する。

採用理由と、ソースで確認した事実（tag 9d20165, 2026-07-27）:

| 要件 | KSafe の実装 | 確認箇所 |
|---|---|---|
| iOS Keychain が iCloud 同期しない | `kSecAttrAccessible*ThisDeviceOnly` を設定し、`kSecAttrSynchronizable` はソース中に一切現れない（既定の false） | `AppleKeychainEncryption.kt:353` |
| iOS が初回アンロック後にアクセス可能 | `requireUnlockedDevice=false` の既定で `kSecAttrAccessibleAfterFirstUnlockThisDeviceOnly` | `AppleKeychainEncryption.kt:354` |
| Android がユーザー認証不要の Keystore 鍵 | `AndroidKeystoreEncryption` が AES-256-GCM の DEK を Keystore 鍵で wrap する | `AndroidKeystoreEncryption.kt` |
| Android の書き込みが atomic | `preferencesDataStoreFile` 経由の DataStore（tmp → rename） | `KSafe.android.kt` |
| minSdk 26 | ライブラリの minSdk は 24 | README 互換性表 |

他候補は次の理由で落ちた。multiplatform-settings は Android の暗号化を自前で用意する必要がある。
KVault は 2024-06 以降更新がなく、iOS 側が `OSStatus` を捨てるため復号失敗を検知できない。
encrypted-datastore は iOS ターゲットを持たない。

### 意図的に満たさない契約

fankt 側 spec `pixiview-secure-session-migration` の
**Requirement: Recoverable secure-storage failure** は、このリリースでは満たさない。

KSafe の `get()` は、一時的な失敗（端末ロック中、Keystore/Keychain が一時的に応答しない）を
例外として再送出する一方、恒久的な破損（`AEADBadTagException`、`KeyPermanentlyInvalidatedException`）
を内部で自己修復し `defaultValue` を返す（`KSafeCoreRead.kt:74` の分岐）。
そのため呼び出し側からは「Cookie が壊れて消えた」と「そもそもログインしていない」が区別できない。

この PR での挙動は次のとおり。

- 恒久的破損 → 未ログインとして扱い、ユーザーは再ログインする
- 一時的失敗 → 例外が伝播するので、payload は保持され次回起動で再試行される

破損と未ログインの区別は follow-up issue に切り出す。

## 構成

```
FanboxRepositoryImpl
   └─ FanboxCookieStorage（Koin で注入）= MigratingFanboxCookieStorage
        ├─ SecureFanboxCookieStorage（destination）
        │    └─ KSafe（Android Keystore / iOS Keychain）
        └─ legacy: RoomFanboxCookieStorage（移行元）
```

すべて `:core:datastore` に置く。このモジュールに `api(libs.fankt.fanbox)` を追加する。

## 保存形式

KSafe に 1 つのキーで、Cookie レコード全件と移行完了マーカーを含む
`@Serializable` なオブジェクトを 1 値として書く。部分的に書かれた状態を作らないため。

```kotlin
@Serializable
internal data class SecureCookiePayload(
    val records: List<SecureCookieRecord>,
    val isMigrationCompleted: Boolean,
)
```

`SecureCookieRecord` は `FanboxCookieRecord` の 7 フィールドをそのまま持つ。

## routing の決定（初回アクセス時に Mutex で 1 回だけ）

```
secure を読む
├─ レコードあり + マーカーあり  → SECURE。Room の cleanup が未了なら試みる
├─ レコードあり + マーカー無し  → SECURE（destination-conflict。Room で上書きしない）
└─ 空
     └─ Room を読む
          ├─ 空          → SECURE（マーカーだけ書く）
          └─ レコードあり → 移行を実行
```

移行の順序は次を厳守する。

```
Room 読み出し
  → secure へ 1 値として suspend put で書き、commit の完了を待つ
  → routing を SECURE へ切替
  → ここまで成功して初めて Room を clear / close
```

commit の完了前に失敗したら routing は LEGACY のままとし、そのプロセスは Room を使い続ける。
次回起動で移行を再試行する。

### read-back 検証を置かない理由

fankt 側 spec は「destination を read-back して 7 フィールドの一致を検証する」ことを求めるが、
KSafe に対しては read-back が検証として機能しない。

`putEncryptedSuspend` は書き込みキューへ送る前に `memoryCache` を楽観的に更新する
（`KSafeCorePutSuspend.kt:127`）。`get()` はその `memoryCache` から読む（`KSafeCoreRead.kt:21`）。
そのため直後の read-back は、ディスクの内容ではなく今書いた値をメモリから返すだけであり、
永続化を確かめられない。

代わりに `put()` の完了そのものを検証とする。`putEncryptedSuspend` は
`CompletableDeferred` を書き込みバッチに載せて `await()` しており
（`KSafeCorePutSuspend.kt:150`）、commit の失敗は `completeExceptionally` で
その `await()` に伝わる（`KSafeCoreWriteConsumer.kt:92`、`KSafeCoreCommit.kt:557`）。
したがって `put()` が例外なく戻ったことが commit 済みの証拠になる。

書いた内容そのものの照合は、Room から読んだレコードと、
secure へ渡す直前のレコードの間で行う（同一プロセス内の値の比較）。

`fankt.db` のファイル自体は削除しない。fankt が削除 API を公開しておらず、
PixiView がパスを組み立てて消すと、他に開いている所有者がいないことを証明できないため。
レコードは `clear()` で消える。

## Cookie 操作の直列化

`MigratingFanboxCookieStorage` のすべての操作は、routing が確定するまで待つ。
移行中に届いた読み書きが、2 つのストアの間で失われないようにするため。

`cookies` Flow は routing 確定後に選ばれた backend の Flow を流す。
確定前でも空リストを 1 度流す。`FanboxRepository.sessionId` と、それを combine する
UI 状態が値を出せなくなるのを防ぐため。

## 併せて直す既存の不具合

`PixiViewViewModel.kt:177-183` の `OldCookieDataStore` 取り込みに 3 点の問題がある。

```kotlin
val oldCookies = oldCookieDataStore.getCookies()
val sessionId = oldCookies.map { it.split("=") }.firstOrNull { it.first() == "FANBOXSESSID" }?.get(1)
if (sessionId != null) {
    fanboxRepository.setSessionId(sessionId)
    oldCookieDataStore.save("")
}
```

1. `split("=")` なので値に `=` を含むと切り詰められる
2. 先頭以外の要素は `; ` 区切りで前に空白が入るため、trim しないと名前が一致しない
3. `setSessionId()` の成功を確認する前に旧ソースを消しているため、書き込みに失敗すると復元元が失われる

読み出し → 書き込み → read-back で同じ値を確認 → 確認できた場合のみ旧ソースを消す、へ直す。

## logout

`logout()` は現在 `setSessionId("")` を呼んでおり、空の `FANBOXSESSID` を保存する。
セッションを削除する形に直し、secure と旧 Room の両方を消してから完了を報告する。
WebView の Cookie 削除に失敗しても、永続ストアの削除は行う。

## Android のバックアップ除外

`data_extraction_rules.xml` と `backup_rules.xml` で、KSafe の DataStore ファイルと
`fankt.db`（sidecar を含む）をクラウドバックアップと端末間転送から除外する。
Keystore の鍵は端末外に出ないため、暗号文だけが転送されても復号できないが、
無意味な復元先を作らないため明示的に除外する。

## telemetry

移行の started / succeeded / fallback-used / failed を記録する。
Cookie の値、payload、および資格情報から導出した識別子は含めない。

## 受け入れ条件（issue #109）

- 既存ユーザーがアプリ更新後もログイン状態を維持している
- 新規ログインが secure store のみに保存される
