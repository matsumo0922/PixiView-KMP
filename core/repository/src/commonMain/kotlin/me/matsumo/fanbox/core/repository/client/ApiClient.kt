package me.matsumo.fanbox.core.repository.client

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.datastore.FanboxCookieDataStore

class ApiClient(
    private val formatter: Json,
    private val cookieDataStore: FanboxCookieDataStore,
) {
    val client = HttpClient {
        install(Logging) {
            level = LogLevel.HEADERS
            logger = object : Logger {
                override fun log(message: String) {
                    Napier.d(message)
                }
            }
        }

        install(HttpCookies) {
            storage = CookiesStorage(cookieDataStore)
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
