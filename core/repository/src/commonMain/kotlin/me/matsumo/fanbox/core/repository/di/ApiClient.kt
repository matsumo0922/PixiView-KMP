package me.matsumo.fanbox.core.repository.di

import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.HttpClientEngineFactory
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import io.ktor.http.renderSetCookieHeader
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.datastore.FanboxCookieDataStore

class ApiClient(
    private val formatter: Json,
    private val cookieDataStore: FanboxCookieDataStore,
) {
    val client = HttpClient {
        val cachedCookies = mutableMapOf<String, Cookie>()

        install(HttpCookies) {
            storage = object : CookiesStorage {
                override suspend fun addCookie(requestUrl: Url, cookie: Cookie) {
                    cachedCookies[requestUrl.toString()] = cookie
                }

                override suspend fun get(requestUrl: Url): List<Cookie> {
                    val url = requestUrl.toString()
                    val cookiesString = cachedCookies[url]?.let { renderSetCookieHeader(it) }

                    if (!cookiesString.isNullOrBlank()) {
                        val cookieHeaders = cookiesString.split(";")
                        val cookies = mutableListOf<Cookie?>()

                        for (header in cookieHeaders) {
                            cookies.add(parseServerSetCookieHeader(header))
                        }

                        cookieDataStore.save(cookiesString)

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
}
