package me.matsumo.fanbox.core.datastore.di

import me.matsumo.fanbox.core.datastore.DummyDataStore
import me.matsumo.fanbox.core.datastore.DummyDataStoreImpl
import me.matsumo.fanbox.core.datastore.PreferenceHelper
import me.matsumo.fanbox.core.datastore.PreferenceHelperImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataStoreHelperModule: Module = module {

    single<PreferenceHelper> {
        PreferenceHelperImpl(
            context = get(),
            ioDispatcher = get(),
        )
    }

    single<DummyDataStore> {
        DummyDataStoreImpl(
            context = get(),
            userDataStore = get(),
        )
    }
}
