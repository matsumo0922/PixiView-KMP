package me.matsumo.fanbox.core.repository

import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import me.matsumo.fanbox.core.datastore.SettingDataStore
import me.matsumo.fankt.fanbox.Fanbox
import me.matsumo.fankt.fanbox.FanboxCookieStorage
import me.matsumo.fankt.fanbox.FanboxGuestCache
import me.matsumo.fankt.fanbox.FanboxGuestEvent
import me.matsumo.fankt.fanbox.FanboxLogLevel
import kotlin.time.Duration.Companion.milliseconds

/**
 * guest の配信先。developer mode の有無で選ぶ。
 *
 * [manifestUrl] のパスに含まれる版は host と guest のあいだの API の版であり、この版のあいだは互換な bundle だけが
 * 配信される。版が上がった配信物は別のパスへ置かれるため、更新前のアプリが解釈できない bundle を受け取ることはない。
 *
 * [cacheDirectoryName] はチャンネルごとに分ける。共有すると、片方の配信先に届かないときに、もう片方からロードした
 * bundle がキャッシュから動いてしまう。
 */
internal enum class GuestChannel(val manifestUrl: String, val cacheDirectoryName: String) {
    /**
     * 手動で昇格した配信物。fankt の `main` へ入った変更は dev チャンネルへ配信され、昇格を実行するまで
     * このパスの内容は変わらない。
     */
    Production(
        manifestUrl = "https://matsumo0922.github.io/fankt/zipline/v1/manifest.zipline.json",
        cacheDirectoryName = "production",
    ),

    /**
     * 昇格前の配信物。fankt の `main` へ入った変更がそのまま配信されるため、検証を経ていない bundle が置かれている。
     * 昇格の前に実機で確かめるためにあり、developer mode を有効にした端末だけが参照する。
     */
    Dev(
        manifestUrl = "https://matsumo0922.github.io/fankt/zipline/v1-dev/manifest.zipline.json",
        cacheDirectoryName = "dev",
    ),
}

/**
 * 参照する配信先を選ぶ。
 *
 * ビルドの種別では分岐しない。`isDebuggable` を持つビルドは debug だけではなく、種別が増えるたびに
 * どちらを読むかを判断し直すことになる。
 */
internal fun guestChannel(isDeveloperMode: Boolean): GuestChannel = if (isDeveloperMode) GuestChannel.Dev else GuestChannel.Production

/** 配信先ごとの guest bundle のキャッシュを作る。保存先は platform ごとに決まる。 */
fun interface GuestCacheFactory {
    /** [directoryName] のディレクトリにキャッシュを置く [FanboxGuestCache] を作る。 */
    fun create(directoryName: String): FanboxGuestCache
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

/**
 * 配信物の署名を検証する Ed25519 公開鍵。鍵名は fankt 側の署名設定と一致している必要がある。
 *
 * `fanboxGuest` は通常の配信に使う主鍵、`fanboxGuestRecovery` は主鍵を失ったときに配信を続けるための
 * 復旧鍵である。どちらかの鍵で署名された manifest を受け付ける。
 *
 * 秘匿を要さないが、リリースへ焼き込まれるためアプリの更新なしには変更できない。配信中の manifest と
 * 対応しない鍵を置くと guest をロードできず、FANBOX の API がすべて失敗する。入れ替える際は、配信中の
 * manifest の署名を実際に検証すること。
 */
internal val GUEST_TRUSTED_KEYS: Map<String, String> = mapOf(
    "fanboxGuest" to "85e4838d32a4e01a5209c7d4267bbe9349608f1227080c2460246f7ea83a0836",
    "fanboxGuestRecovery" to "5279962d35d36dcbbeea47a25612aa5708ebfe0967cbcc18314ef919d55dcb5e",
)

/**
 * 配信先と公開鍵を渡して [Fanbox] を生成する。
 *
 * FANBOX の request の組み立てと response の解析は、配信された guest で実行される。FANBOX の仕様変更への
 * 追従は、アプリの更新を経ずに配信で届く。配信先へ到達できないときは、署名を検証済みのキャッシュから
 * ロードする。キャッシュも無いときは、この [Fanbox] の API がすべて `FanboxException.GuestFailure` で
 * 失敗する。
 */
@Suppress("LongParameterList")
internal fun createFanbox(
    logLevel: FanboxLogLevel,
    settingDataStore: SettingDataStore,
    ioDispatcher: CoroutineDispatcher,
    cookieStorage: FanboxCookieStorage,
    guestCacheFactory: GuestCacheFactory,
    guestEventListener: (FanboxGuestEvent) -> Unit,
): Fanbox {
    val isDeveloperMode = loadDeveloperMode { settingDataStore.loadStoredSetting().isDeveloperMode }
    val channel = guestChannel(isDeveloperMode)

    return Fanbox(
        guestManifestUrl = channel.manifestUrl,
        guestTrustedKeys = GUEST_TRUSTED_KEYS.mapValues { (_, key) -> key.hexToByteArray() },
        guestCache = guestCacheFactory.create(channel.cacheDirectoryName),
        guestEventListener = guestEventListener,
        logLevel = logLevel,
        ioDispatcher = ioDispatcher,
        cookieStorage = cookieStorage,
    )
}
