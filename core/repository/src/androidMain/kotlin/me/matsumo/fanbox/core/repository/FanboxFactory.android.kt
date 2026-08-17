package me.matsumo.fanbox.core.repository

import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.matsumo.fanbox.core.common.util.recordException
import me.matsumo.fanbox.core.datastore.SettingDataStore
import me.matsumo.fankt.fanbox.Fanbox
import me.matsumo.fankt.fanbox.FanboxCookieStorage
import me.matsumo.fankt.fanbox.FanboxLogLevel
import kotlin.time.Duration.Companion.milliseconds

/**
 * 投稿詳細の解析処理を配信する manifest の所在。
 *
 * ここへ置かれるのは手動で昇格した配信物だけである。fankt の `main` へ入った変更は dev チャンネルへ
 * 配信され、昇格を実行するまでこのパスの内容は変わらない。
 *
 * パスに含まれる版は host と guest のあいだの API の版であり、この版のあいだは互換な bundle だけが
 * 配信される。版が上がった配信物は別のパスへ置かれるため、更新前のアプリが解釈できない bundle を
 * 受け取ることはない。
 */
private const val GUEST_MANIFEST_URL =
    "https://matsumo0922.github.io/fankt/zipline/v1/manifest.zipline.json"

/**
 * 昇格前の配信物が置かれる manifest の所在。
 *
 * fankt の `main` へ入った変更がそのまま配信されるため、検証を経ていない bundle が置かれている。
 * 昇格の前に実機で確かめるためにあり、developer mode を有効にした端末だけが参照する。
 */
private const val GUEST_DEV_MANIFEST_URL =
    "https://matsumo0922.github.io/fankt/zipline/v1-dev/manifest.zipline.json"

/**
 * 参照する配信先を選ぶ。
 *
 * ビルドの種別では分岐しない。`isDebuggable` を持つビルドは debug だけではなく、種別が増えるたびに
 * どちらを読むかを判断し直すことになる。
 */
internal fun guestManifestUrl(isDeveloperMode: Boolean): String {
    return if (isDeveloperMode) GUEST_DEV_MANIFEST_URL else GUEST_MANIFEST_URL
}

/**
 * 保存済みの設定を読む上限。
 *
 * 読み取りは最初のコンポジションを行う thread をブロックする。単一の preferences ファイルの読み取りは
 * 通常これより 2 桁短い時間で終わるため、上限に達するのは病的に遅いストレージに限られる。打ち切った
 * 場合に失うのは開発者向けの配信先の切り替えだけで、起動そのものは続く。
 */
private val STORED_SETTING_READ_TIMEOUT = 500.milliseconds

/**
 * 保存済みの developer mode を読む。読めなければ無効として扱う。
 *
 * [Fanbox] の生成は同期的に起き、`SettingDataStore.setting` は購読が始まるまで既定値を返すため、
 * 保存済みの値をここで待つ。この待ちは最初のコンポジションを行う thread で起きるので上限を設ける。
 *
 * 失敗と打ち切りのいずれも「無効」へ倒す。未検証の配信物を実行しない側が安全であり、この向きなら
 * 読み取りの不調は、開発者向けの切り替えが効かないことに留まる。
 *
 * 読み取りそのものを [load] として受け取るのは、失敗と打ち切りの経路をテストから通せるようにするため。
 */
internal fun loadDeveloperMode(load: suspend () -> Boolean): Boolean = runCatching {
    runBlocking {
        withTimeoutOrNull(STORED_SETTING_READ_TIMEOUT) { load() }
    }
}.getOrElse { failure ->
    Napier.w(failure) { "Failed to read the stored developer mode; treating it as disabled." }
    null
} ?: false

/** 配信物の署名に使う鍵の名前。fankt 側の署名設定と一致している必要がある。 */
private const val GUEST_TRUSTED_KEY_NAME = "fanboxGuest"

/**
 * 配信物の署名を検証する Ed25519 公開鍵。
 *
 * 秘匿を要さないが、リリースへ焼き込まれるためアプリの更新なしには変更できない。入れ替える際は、
 * 移行期に新旧の双方で署名した manifest を配信し、旧鍵を埋め込んだリリースが使われなくなってから
 * 旧鍵を廃止する。
 *
 * 配信中の manifest と対応しない鍵を置くと、guest は一度も起動しないまま直接経路で動作し続ける。
 * 症状が「従来どおり動く」であるため、入れ替えの際は配信中の manifest の署名を実際に検証すること。
 */
internal const val GUEST_TRUSTED_ED25519_PUBLIC_KEY_HEX =
    "5c45794e6a3c11de4ad637bbfc1714071eb3c8a9bb7139f4a3f88dc75a36146e"

/**
 * guest を起動しないことを指示する Remote Config のキー。
 *
 * 「停止」の向きで定義している。Remote Config は活性化済みの値も既定値も無い boolean に `false` を
 * 返すため、この向きなら値を一度も取得できていない端末がそのまま「停止していない」になる。既定値の
 * 登録が要らず、その完了を待たずに値を読む競合も生じない。
 *
 * 綴りが Firebase コンソールの設定と食い違うと、症状は「フラグを倒しても止まらない」という無言の
 * 失敗になる。変更する際はコンソールの表示と実際に照合すること。
 */
private const val GUEST_ROUTE_KILL_SWITCH_KEY = "android_guest_route_kill_switch"

/**
 * Remote Config を取得しに行く最短の間隔。
 *
 * Firebase の既定は 12 時間で、停止を決めてから端末へ届くまでの遅延がそのぶん延びる。停止スイッチは
 * 障害対応の道具であるため 1 時間へ縮める。
 */
private const val REMOTE_CONFIG_FETCH_INTERVAL_SECONDS = 3600L

/**
 * 停止フラグが立っていない限り、配信先と公開鍵を渡して [Fanbox] を生成する。これにより投稿詳細の解析が
 * 配信済みの guest で実行され、FANBOX の仕様変更への追従がアプリの更新を経ずに届く。配信先へ到達
 * できない場合は fankt が guest を経由しない直接経路へ退避する。
 *
 * 停止フラグが立っている場合は配信先も公開鍵も渡さない。配信先への取得そのものが起きないため、配信元を
 * 操作できない場合や manifest の取得が返らない場合でも guest を止められる。
 */
internal actual fun createFanbox(
    logLevel: FanboxLogLevel,
    settingDataStore: SettingDataStore,
    ioDispatcher: CoroutineDispatcher,
    cookieStorage: FanboxCookieStorage,
): Fanbox {
    if (isGuestRouteKilled()) {
        return Fanbox(
            logLevel = logLevel,
            ioDispatcher = ioDispatcher,
            cookieStorage = cookieStorage,
        )
    }

    val isDeveloperMode = loadDeveloperMode { settingDataStore.loadStoredSetting().isDeveloperMode }

    return Fanbox(
        guestManifestUrl = guestManifestUrl(isDeveloperMode),
        guestTrustedKeyName = GUEST_TRUSTED_KEY_NAME,
        guestTrustedEd25519PublicKey = GUEST_TRUSTED_ED25519_PUBLIC_KEY_HEX.hexToByteArray(),
        logLevel = logLevel,
        ioDispatcher = ioDispatcher,
        cookieStorage = cookieStorage,
    )
}

/**
 * 活性化済みの停止フラグを読む。取得は背景で始め、取得した値は次回の起動で読まれる。
 *
 * 参照が失敗した場合は guest を起動する側へ倒す。guest 経路は「どの失敗も直接経路へ退避する」で
 * 一貫しており、停止スイッチ自身がアプリを落とすとこの一貫性が反転して Firebase の不調がアプリの
 * 起動不能になる。記録の呼び出しも守るのは、記録先の Crashlytics が Remote Config と同じ既定の
 * FirebaseApp を要し、参照が失敗する状況では記録もまた失敗するため。
 */
private fun isGuestRouteKilled(): Boolean = runCatching {
    val remoteConfig = Firebase.remoteConfig

    remoteConfig.setConfigSettingsAsync(
        remoteConfigSettings {
            minimumFetchIntervalInSeconds = REMOTE_CONFIG_FETCH_INTERVAL_SECONDS
        },
    ).addOnCompleteListener { remoteConfig.fetchAndActivate() }

    remoteConfig.getBoolean(GUEST_ROUTE_KILL_SWITCH_KEY)
}.getOrElse { failure ->
    runCatching { recordException(failure) }
    false
}
