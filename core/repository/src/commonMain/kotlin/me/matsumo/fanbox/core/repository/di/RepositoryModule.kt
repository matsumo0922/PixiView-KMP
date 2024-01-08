package me.matsumo.fanbox.core.repository.di

import io.ktor.client.HttpClient
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.repository.FanboxRepositoryImpl
import me.matsumo.fanbox.core.repository.UserDataRepositoryImpl
import org.koin.dsl.module

@OptIn(ExperimentalSerializationApi::class)
val repositoryModule = module {

    single {
        Json {
            isLenient = true
            prettyPrint = true
            ignoreUnknownKeys = true
            coerceInputValues = true
            encodeDefaults = true
            explicitNulls = false
        }
    }

    single<HttpClient> {
        ApiClient(
            formatter = get(),
            cookieDataStore = get(),
        ).client
    }

    single {
        UserDataRepositoryImpl(
            pixiViewDataStore = get(),
        )
    }

    single {
        FanboxRepositoryImpl(
            client = get(),
            formatter = get(),
            fanboxCookieDataStore = get(),
            bookmarkDataStore = get(),
            ioDispatcher = get(),
        )
    }
}
