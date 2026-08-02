package me.matsumo.fanbox.feature.welcome.web

import me.matsumo.fankt.fanbox.FanboxCookieRecord
import com.multiplatform.webview.cookie.Cookie as WebViewCookie

/**
 * WebView の Cookie を fankt の保存形式へ変換する。
 *
 * [nowEpochMilliseconds] は `maxAge` を絶対時刻へ直すための基準時刻である。
 *
 * WebView 側の `domain` と `path` は、Android で `GET_COOKIE_INFO` に対応していない端末では null に
 * なる。その場合は [FANBOX_COOKIE_DOMAIN] を補う。Cookie を取得しているのは `www.fanbox.cc` だが
 * API は `api.fanbox.cc` へ送るため、取得元ホストで補うと FANBOXSESSID がリクエストに載らなくなる。
 *
 * `hostOnly` は常に false とする。FANBOX の認証 Cookie はサブドメインをまたいで使う必要があり、
 * fankt の Room storage も schema v3 で全レコードをドメイン Cookie として保存するため、ここで true を
 * 立てても永続化を経た時点で false に戻る。
 */
internal fun List<WebViewCookie>.toFanboxCookieRecords(nowEpochMilliseconds: Long): List<FanboxCookieRecord> {
    return map { cookie ->
        FanboxCookieRecord(
            domain = cookie.domain ?: FANBOX_COOKIE_DOMAIN,
            path = cookie.path ?: "/",
            name = cookie.name,
            value = cookie.value,
            expiresAtEpochMilliseconds = cookie.expiresAtEpochMilliseconds(nowEpochMilliseconds),
            secure = cookie.isSecure ?: false,
            hostOnly = false,
        )
    }
}

/**
 * Cookie の有効期限を、fankt が要求する絶対時刻のミリ秒として返す。
 *
 * WebView 側の `expiresDate` はプラットフォームで単位が揃っていない。iOS は `timeIntervalSince1970`
 * の秒をそのまま入れ、Android は `Expires` 属性由来ならミリ秒、`Max-Age` 属性由来なら現在時刻に相対秒
 * をそのまま足した値を入れる。秒とミリ秒では 3 桁違い、秒として解釈すべき値をミリ秒とみなすと必ず
 * 過去の時刻になる。fankt 0.1.0 は期限切れの Cookie を保存時に落とすため、そのまま渡すとログイン
 * 直後にセッションが消える。
 *
 * 値の大きさで単位を判定する。[SECONDS_MILLISECONDS_BOUNDARY] 未満ならミリ秒としては 2001 年より前を
 * 指すことになり、Cookie の有効期限としては現れない。その場合は秒とみなして 1000 倍する。
 *
 * `expiresDate` が無く `maxAge` だけがある場合は、[nowEpochMilliseconds] に相対秒を加えて絶対値に
 * する。どちらも無い Cookie はセッション Cookie であり、期限なしとして扱う。
 */
private fun WebViewCookie.expiresAtEpochMilliseconds(nowEpochMilliseconds: Long): Long? {
    val rawExpiresDate = expiresDate
        ?: return maxAge?.let { nowEpochMilliseconds + it * MILLISECONDS_PER_SECOND }

    return if (rawExpiresDate < SECONDS_MILLISECONDS_BOUNDARY) {
        rawExpiresDate * MILLISECONDS_PER_SECOND
    } else {
        rawExpiresDate
    }
}

/** Cookie の domain 属性が取れない場合に補うドメイン。サブドメインにも送られる。 */
internal const val FANBOX_COOKIE_DOMAIN: String = "fanbox.cc"

private const val MILLISECONDS_PER_SECOND: Long = 1000L

/** 2001-09-09T01:46:40Z。これ未満の値はミリ秒ではなく秒とみなす。 */
private const val SECONDS_MILLISECONDS_BOUNDARY: Long = 1_000_000_000_000L
