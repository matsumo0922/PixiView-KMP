package me.matsumo.fanbox.core.datastore.di

import me.matsumo.fanbox.core.datastore.PreferenceHelperImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataStoreHelperModule: Module = module {

    single<PreferenceHelperImpl> {
        PreferenceHelperImpl(
            context = get(),
            ioDispatcher = get(),
        )
    }
}
