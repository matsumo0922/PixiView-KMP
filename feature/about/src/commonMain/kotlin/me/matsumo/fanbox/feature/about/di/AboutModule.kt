package me.matsumo.fanbox.feature.about.di

import me.matsumo.fanbox.feature.about.about.AboutViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

val aboutModule = module {

    factory {
        AboutViewModel(
            pixiViewConfig = get(),
            userDataRepository = get(),
        )
    }

    includes(aboutSubModule)
}

expect val aboutSubModule: Module
