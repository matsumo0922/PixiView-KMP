package me.matsumo.fanbox.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import me.matsumo.fanbox.BuildKonfig
import me.matsumo.fanbox.PixiViewViewModel
import me.matsumo.fanbox.core.common.PixiViewConfig
import org.koin.dsl.module

expect fun getPixiViewConfig(): PixiViewConfig

val appModule = module {

    single {
        getPixiViewConfig()
    }

    single<CoroutineDispatcher> {
        Dispatchers.IO
    }

    factory { 
        PixiViewViewModel(
            userDataRepository = get(),
            fanboxRepository = get(),
            billingStatus = get(),
        )
    }
}
