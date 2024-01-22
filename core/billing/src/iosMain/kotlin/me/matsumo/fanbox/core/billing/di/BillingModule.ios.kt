package me.matsumo.fanbox.core.billing.di

import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.BillingClientImpl
import me.matsumo.fanbox.core.billing.BillingInitialize
import me.matsumo.fanbox.core.billing.BillingInitializeImpl
import me.matsumo.fanbox.core.billing.usecase.PurchasePlusSubscriptionUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val billingModule: Module = module {

    single<BillingInitialize> {
        BillingInitializeImpl(
            userDataRepository = get(),
            ioDispatcher = get()
        )
    }

    single<BillingClient> { BillingClientImpl() }

    factory {
        PurchasePlusSubscriptionUseCase(
            billingClient = get()
        )
    }
}
