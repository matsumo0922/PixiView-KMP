package me.matsumo.fanbox.core.repository.di

import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.HttpRequestRetry
import io.ktor.client.plugins.HttpResponseValidator
import io.ktor.client.plugins.RedirectResponseException
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.cookies.HttpCookies
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.FanboxRepositoryImpl
import me.matsumo.fanbox.core.repository.RewardRepository
import me.matsumo.fanbox.core.repository.RewardRepositoryImpl
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.repository.UserDataRepositoryImpl
import me.matsumo.fanbox.core.repository.client.CookiesStorage
import org.koin.core.module.Module
import org.koin.core.qualifier.StringQualifier
import org.koin.core.qualifier.named
import org.koin.dsl.module

enum class ClientType(val named: StringQualifier) {
    DEFAULT(named("DEFAULT")),
    CONTENT_NEGOTIATION(named("CONTENT_NEGOTIATION")),
}

val json = Json {
    isLenient = true
    prettyPrint = true
    ignoreUnknownKeys = true
    coerceInputValues = true
    encodeDefaults = true
    explicitNulls = false
}

expect val repositorySubModule: Module

val repositoryModule = module {
    single {
        json
    }

    single<CookiesStorage> {
        CookiesStorage(get())
    }

    single<HttpClient>(ClientType.DEFAULT.named) {
        HttpClient {
            useDefaultTransformers = false

            install(Logging) {
                level = LogLevel.ALL
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message)
                    }
                }
            }

            install(HttpCookies) {
                storage = get<CookiesStorage>()
            }
        }
    }

    single<HttpClient>(ClientType.CONTENT_NEGOTIATION.named) {
        HttpClient {
            HttpResponseValidator {
                validateResponse { response ->
                    if (response.call.request.url.toString().contains("https://www.fanbox.cc") && response.status.value in 300..399) {
                        throw RedirectResponseException(response, "Redirect is not allowed.")
                    }
                }
            }

            install(Logging) {
                level = LogLevel.INFO
                logger = object : Logger {
                    override fun log(message: String) {
                        Napier.d(message)
                    }
                }
            }

            install(HttpCookies) {
                storage = get<CookiesStorage>()
            }

            install(ContentNegotiation) {
                json(get<Json>())
            }

            install(HttpRequestRetry) {
                maxRetries = 1

                retryIf { _, response ->
                    !response.status.isSuccess()
                }

                exponentialDelay()
            }
        }
    }

    single<UserDataRepository> {
        UserDataRepositoryImpl(
            pixiViewDataStore = get(),
        )
    }

    single<FanboxRepository> {
        FanboxRepositoryImpl(
            client = get(ClientType.CONTENT_NEGOTIATION.named),
            defaultClient = get(ClientType.DEFAULT.named),
            formatter = get(),
            fanboxCookieDataStore = get(),
            bookmarkDataStore = get(),
            blockDataStore = get(),
            userDataStore = get(),
            ioDispatcher = get(),
        )
    }

    single<RewardRepository> {
        RewardRepositoryImpl(
            rewardLogDataStore = get(),
            ioDispatcher = get(),
        )
    }
}
