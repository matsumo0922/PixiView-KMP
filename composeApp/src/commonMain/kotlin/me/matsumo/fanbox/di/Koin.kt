package me.matsumo.fanbox.di

import me.matsumo.fanbox.core.billing.di.billingModule
import me.matsumo.fanbox.core.datastore.di.dataStoreHelperModule
import me.matsumo.fanbox.core.datastore.di.dataStoreModule
import me.matsumo.fanbox.core.logs.di.logModule
import me.matsumo.fanbox.core.repository.di.repositoryModule
import me.matsumo.fanbox.core.ui.di.uiSubModule
import me.matsumo.fanbox.feature.about.di.aboutModule
import me.matsumo.fanbox.feature.creator.di.creatorModule
import me.matsumo.fanbox.feature.library.di.libraryModule
import me.matsumo.fanbox.feature.post.di.postModule
import me.matsumo.fanbox.feature.setting.di.settingModule
import me.matsumo.fanbox.feature.welcome.di.welcomeModule
import org.koin.core.KoinApplication

fun KoinApplication.applyModules() {
    modules(appModule)

    modules(dataStoreModule)
    modules(dataStoreHelperModule)
    modules(repositoryModule)
    modules(billingModule)
    modules(uiSubModule)
    modules(logModule)

    modules(welcomeModule)
    modules(libraryModule)
    modules(settingModule)
    modules(aboutModule)
    modules(postModule)
    modules(creatorModule)
}
