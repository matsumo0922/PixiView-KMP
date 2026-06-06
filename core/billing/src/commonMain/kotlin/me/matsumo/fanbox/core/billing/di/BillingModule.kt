package me.matsumo.fanbox.core.billing.di

import me.matsumo.fanbox.core.billing.BillingClient
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val billingModule: Module = module {
    singleOf(::BillingClient)
}
