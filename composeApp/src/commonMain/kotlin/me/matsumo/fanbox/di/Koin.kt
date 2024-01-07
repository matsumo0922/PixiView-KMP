package me.matsumo.fanbox.di

import me.matsumo.fanbox.core.datastore.di.dataStoreHelperModule
import me.matsumo.fanbox.core.datastore.di.dataStoreModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(others: KoinApplication.() -> Unit = {}) {
    startKoin {
        modules(appModule)
        modules(dataStoreModule)
        modules(dataStoreHelperModule)

        others.invoke(this)
    }
}
