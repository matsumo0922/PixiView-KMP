package me.matsumo.fanbox.core.datastore.di

import me.matsumo.fanbox.core.datastore.BlockDataStore
import me.matsumo.fanbox.core.datastore.BookmarkDataStore
import me.matsumo.fanbox.core.datastore.FlagDataStore
import me.matsumo.fanbox.core.datastore.LaunchLogDataStore
import me.matsumo.fanbox.core.datastore.OldCookieDataStore
import me.matsumo.fanbox.core.datastore.PixiViewDataStore
import me.matsumo.fanbox.core.datastore.RewardLogDataStore
import org.koin.core.module.Module
import org.koin.dsl.module

expect val dataStoreHelperModule: Module

val dataStoreModule = module {

    single {
        PixiViewDataStore(
            preferenceHelper = get(),
            formatter = get(),
            ioDispatcher = get(),
        )
    }

    single {
        BookmarkDataStore(
            preferenceHelper = get(),
            ioDispatcher = get(),
        )
    }

    single {
        BlockDataStore(
            preferenceHelper = get(),
            ioDispatcher = get(),
        )
    }

    single {
        LaunchLogDataStore(
            preferenceHelper = get(),
            ioDispatcher = get(),
        )
    }

    single {
        RewardLogDataStore(
            preferenceHelper = get(),
            ioDispatcher = get(),
        )
    }

    single {
        OldCookieDataStore(
            preferenceHelper = get(),
        )
    }

    single {
        FlagDataStore(
            preferenceHelper = get(),
        )
    }
}
