package me.matsumo.fanbox.feature.about.di

import me.matsumo.fanbox.feature.about.about.AboutViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.core.module.Module
import org.koin.dsl.module

val aboutModule = module {
    viewModelOf(::AboutViewModel)
    includes(aboutSubModule)
}

expect val aboutSubModule: Module
