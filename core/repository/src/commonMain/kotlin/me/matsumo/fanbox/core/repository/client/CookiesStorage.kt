package me.matsumo.fanbox.core.repository.client

import io.ktor.client.plugins.cookies.CookiesStorage
import io.ktor.http.Cookie
import io.ktor.http.Url
import io.ktor.http.parseServerSetCookieHeader
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import me.matsumo.fanbox.core.datastore.FanboxCookieDataStore

class CookiesStorage(
    private val cookieDataStore: FanboxCookieDataStore,
): CookiesStorage {

    private val mutex = Mutex()

    override suspend fun addCookie(requestUrl: Url, cookie: Cookie): Unit = mutex.withLock {
        cookieDataStore.addCookies(listOf("${cookie.name}=${cookie.value}"))
    }

    override suspend fun get(requestUrl: Url): List<Cookie> = mutex.withLock {
        if (requestUrl.toString().contains("https://www.fanbox.cc")) return@withLock emptyList()
        cookieDataStore.getCookies().map { parseServerSetCookieHeader(it) }
    }

    override fun close() {
        /* do nothing */
    }
}
