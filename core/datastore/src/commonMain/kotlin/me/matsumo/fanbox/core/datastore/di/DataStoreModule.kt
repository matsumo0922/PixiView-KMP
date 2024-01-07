package me.matsumo.fanbox.core.datastore.di

import me.matsumo.fanbox.core.datastore.FanboxCookieDataStore
import me.matsumo.fanbox.core.datastore.BookmarkDataStore
import me.matsumo.fanbox.core.datastore.PixiViewDataStore
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
        FanboxCookieDataStore(
            preferenceHelper = get(),
        )
    }
}
