package me.matsumo.fanbox.core.repository.di

import io.ktor.client.HttpClient
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.FanboxRepositoryImpl
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.repository.UserDataRepositoryImpl
import me.matsumo.fanbox.core.repository.client.ApiClient
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

    single<UserDataRepository> {
        UserDataRepositoryImpl(
            pixiViewDataStore = get(),
        )
    }

    single<FanboxRepository> {
        FanboxRepositoryImpl(
            client = get(),
            formatter = get(),
            fanboxCookieDataStore = get(),
            bookmarkDataStore = get(),
            ioDispatcher = get(),
        )
    }
}
