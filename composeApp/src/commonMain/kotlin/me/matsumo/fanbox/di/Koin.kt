package me.matsumo.fanbox.di

import me.matsumo.fanbox.core.datastore.di.dataStoreHelperModule
import me.matsumo.fanbox.core.datastore.di.dataStoreModule
import me.matsumo.fanbox.core.repository.di.repositoryModule
import me.matsumo.fanbox.feature.welcome.di.welcomeModule
import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun KoinApplication.applyModules() {
    modules(appModule)

    modules(dataStoreModule)
    modules(dataStoreHelperModule)
    modules(repositoryModule)

    modules(welcomeModule)
}
