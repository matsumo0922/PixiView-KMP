package me.matsumo.fanbox.core.repository.di

import com.multiplatform.webview.cookie.WebViewCookieManager
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.datastore.FanboxCookieDataStore
import com.multiplatform.webview.cookie.Cookie as WebViewCookie

class ApiClient(
    private val formatter: Json,
    private val cookieDataStore: FanboxCookieDataStore,
) {
    val client = HttpClient {
        val cookieManager = WebViewCookieManager()

        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.d(message)
                }
            }
        }

        install(HttpCookies) {
            storage = object : CookiesStorage {
                override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
                    cookieManager.setCookie(requestUrl.toString(), cookie.toWebViewCookie())
                }

                override suspend fun get(requestUrl: Url): List<Cookie> {
                    val url = requestUrl.toString()
                    val cookieHeaders = cookieManager.getCookies(url)

                    if (cookieHeaders.isNotEmpty()) {
                        val cookies = mutableListOf<Cookie?>()

                        for (header in cookieHeaders) {
                            cookies.add(parseServerSetCookieHeader(header.toString()))
                        }

                        cookieDataStore.save(cookies.joinToString(";"))

                        return cookies.filterNotNull()
                    }

                    return emptyList()
                }

                override fun close() {
                    /* do nothing */
                }
            }
        }

        install(ContentNegotiation) {
            json(formatter)
        }

        /*install(HttpRequestRetry) {
            retryOnExceptionIf(maxRetries = 3) { _, throwable -> throwable is UnknownHostException }
            exponentialDelay()
        }*/
    }

    private fun Cookie.toWebViewCookie(): WebViewCookie {
        return WebViewCookie(
            name = name,
            value = value,
            domain = domain,
            path = path,
            expiresDate = expires?.timestamp,
            isSecure = secure,
            isHttpOnly = httpOnly,
            maxAge = maxAge.toLong(),
        )
    }
}
