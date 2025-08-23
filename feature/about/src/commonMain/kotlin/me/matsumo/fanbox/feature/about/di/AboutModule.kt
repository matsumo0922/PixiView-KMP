package me.matsumo.fanbox.feature.about.di

import me.matsumo.fanbox.feature.about.about.AboutViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutModule = module {
    viewModelOf(::AboutViewModel)
}
