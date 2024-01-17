package me.matsumo.fanbox.core.billing.di

import me.matsumo.fanbox.core.billing.BillingInitializeImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val billingModule: Module = module {

    single<BillingInitialize> { BillingInitializeImpl() }
}
