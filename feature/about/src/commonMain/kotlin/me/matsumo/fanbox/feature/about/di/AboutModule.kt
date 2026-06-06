package me.matsumo.fanbox.feature.about.di

import me.matsumo.fanbox.feature.about.about.AboutViewModel
import me.matsumo.fanbox.feature.about.billing.BillingViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val aboutModule = module {
    viewModelOf(::AboutViewModel)
    viewModelOf(::BillingViewModel)
}
