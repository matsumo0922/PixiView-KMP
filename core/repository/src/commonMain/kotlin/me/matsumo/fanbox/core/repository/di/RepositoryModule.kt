package me.matsumo.fanbox.core.repository.di

import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.FanboxRepositoryImpl
import me.matsumo.fanbox.core.repository.FlagRepository
import me.matsumo.fanbox.core.repository.FlagRepositoryImpl
import me.matsumo.fanbox.core.repository.LaunchLogRepository
import me.matsumo.fanbox.core.repository.RewardRepository
import me.matsumo.fanbox.core.repository.RewardRepositoryImpl
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.repository.SettingRepositoryImpl
import me.matsumo.fanbox.core.repository.TranslationRepository
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

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

    single<SettingRepository> {
        SettingRepositoryImpl(
            settingDataStore = get(),
        )
    }

    single<FanboxRepository> {
        FanboxRepositoryImpl(
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

    single<FlagRepository> {
        FlagRepositoryImpl(
            flagDataStore = get(),
        )
    }

    single {
        TranslationRepository(
            formatter = get(),
            pixiViewConfig = get(),
        )
    }

    singleOf(::LaunchLogRepository)
}
