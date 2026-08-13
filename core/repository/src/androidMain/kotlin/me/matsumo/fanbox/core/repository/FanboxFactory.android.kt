package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.CoroutineDispatcher
import me.matsumo.fankt.fanbox.Fanbox
import me.matsumo.fankt.fanbox.FanboxCookieStorage
import me.matsumo.fankt.fanbox.FanboxLogLevel

/**
 * 投稿詳細の解析処理を配信する manifest の所在。
 *
 * パスに含まれる版は host と guest のあいだの API の版であり、この版のあいだは互換な bundle だけが
 * 配信される。版が上がった配信物は別のパスへ置かれるため、更新前のアプリが解釈できない bundle を
 * 受け取ることはない。
 */
private const val GUEST_MANIFEST_URL =
    "https://matsumo0922.github.io/fankt/zipline/v1/manifest.zipline.json"

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
 * 配信先と公開鍵を渡して [Fanbox] を生成する。これにより投稿詳細の解析が配信済みの guest で実行され、
 * FANBOX の仕様変更への追従がアプリの更新を経ずに届く。配信先へ到達できない場合は fankt が guest を
 * 経由しない直接経路へ退避する。
 */
internal actual fun createFanbox(
    logLevel: FanboxLogLevel,
    ioDispatcher: CoroutineDispatcher,
    cookieStorage: FanboxCookieStorage,
): Fanbox {
    return Fanbox(
        guestManifestUrl = GUEST_MANIFEST_URL,
        guestTrustedKeyName = GUEST_TRUSTED_KEY_NAME,
        guestTrustedEd25519PublicKey = GUEST_TRUSTED_ED25519_PUBLIC_KEY_HEX.hexToByteArray(),
        logLevel = logLevel,
        ioDispatcher = ioDispatcher,
        cookieStorage = cookieStorage,
    )
}
