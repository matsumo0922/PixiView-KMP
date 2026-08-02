package me.matsumo.fanbox.feature.welcome.web

import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import com.multiplatform.webview.cookie.Cookie as WebViewCookie

/**
 * WebView の Cookie を fankt の保存形式へ変換する処理を検証するテスト。
 *
 * `expiresDate` の単位はプラットフォームで揃っておらず、iOS は秒、Android は `Expires` 属性由来なら
 * ミリ秒、`Max-Age` 属性由来なら「現在時刻のミリ秒 + 相対秒」を返す。秒をミリ秒として扱うと必ず過去の
 * 時刻になり、fankt が保存時に落とすためログイン直後にセッションが消える。
 */
class WebViewCookieConverterTest {

    /** 2026-08-02T00:00:00Z 相当。 */
    private val now = 1_785_628_800_000L

    private fun cookie(
        name: String = "FANBOXSESSID",
        value: String = "session-value",
        domain: String? = ".fanbox.cc",
        path: String? = "/",
        expiresDate: Long? = null,
        maxAge: Long? = null,
        isSecure: Boolean? = true,
    ) = WebViewCookie(
        name = name,
        value = value,
        domain = domain,
        path = path,
        expiresDate = expiresDate,
        maxAge = maxAge,
        isSecure = isSecure,
    )

    @Test
    fun millisecondExpiryIsKept() {
        val expiresAtMilliseconds = now + 30L * 24 * 60 * 60 * 1000

        val record = listOf(cookie(expiresDate = expiresAtMilliseconds)).toFanboxCookieRecords(now).single()

        assertEquals(expiresAtMilliseconds, record.expiresAtEpochMilliseconds)
    }

    @Test
    fun secondExpiryIsConvertedToMilliseconds() {
        val expiresAtSeconds = (now + 30L * 24 * 60 * 60 * 1000) / 1000

        val record = listOf(cookie(expiresDate = expiresAtSeconds)).toFanboxCookieRecords(now).single()

        assertEquals(expiresAtSeconds * 1000, record.expiresAtEpochMilliseconds)
    }

    @Test
    fun secondExpiryStaysInTheFuture() {
        val expiresAtSeconds = (now + 30L * 24 * 60 * 60 * 1000) / 1000

        val record = listOf(cookie(expiresDate = expiresAtSeconds)).toFanboxCookieRecords(now).single()

        assertEquals(true, record.expiresAtEpochMilliseconds!! > now)
    }

    @Test
    fun maxAgeIsConvertedToAbsoluteMilliseconds() {
        val record = listOf(cookie(expiresDate = null, maxAge = 3600L)).toFanboxCookieRecords(now).single()

        assertEquals(now + 3_600_000L, record.expiresAtEpochMilliseconds)
    }

    @Test
    fun expiresDateWinsOverMaxAge() {
        val expiresAtMilliseconds = now + 30L * 24 * 60 * 60 * 1000

        val record = listOf(cookie(expiresDate = expiresAtMilliseconds, maxAge = 60L))
            .toFanboxCookieRecords(now)
            .single()

        assertEquals(expiresAtMilliseconds, record.expiresAtEpochMilliseconds)
    }

    @Test
    fun sessionCookieHasNoExpiry() {
        val record = listOf(cookie(expiresDate = null, maxAge = null)).toFanboxCookieRecords(now).single()

        assertNull(record.expiresAtEpochMilliseconds)
    }

    @Test
    fun missingDomainFallsBackToTheSharedFanboxDomain() {
        val record = listOf(cookie(domain = null)).toFanboxCookieRecords(now).single()

        assertEquals(FANBOX_COOKIE_DOMAIN, record.domain)
    }

    @Test
    fun missingPathFallsBackToRoot() {
        val record = listOf(cookie(path = null)).toFanboxCookieRecords(now).single()

        assertEquals("/", record.path)
    }

    @Test
    fun cookiesAreAlwaysStoredAsDomainCookies() {
        val records = listOf(cookie(domain = ".fanbox.cc"), cookie(domain = null))
            .toFanboxCookieRecords(now)

        assertEquals(listOf(false, false), records.map { it.hostOnly })
    }

    @Test
    fun nameValueAndSecureArePreserved() {
        val record = listOf(cookie(name = "n", value = "v", isSecure = true)).toFanboxCookieRecords(now).single()

        assertEquals("n", record.name)
        assertEquals("v", record.value)
        assertEquals(true, record.secure)
    }

    @Test
    fun missingSecureIsTreatedAsInsecure() {
        val record = listOf(cookie(isSecure = null)).toFanboxCookieRecords(now).single()

        assertEquals(false, record.secure)
    }

    @Test
    fun everyCookieIsConverted() {
        val records = listOf(cookie(name = "a"), cookie(name = "b"), cookie(name = "c"))
            .toFanboxCookieRecords(now)

        assertEquals(listOf("a", "b", "c"), records.map { it.name })
    }
}
