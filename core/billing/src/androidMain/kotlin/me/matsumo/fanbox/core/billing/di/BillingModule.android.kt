package me.matsumo.fanbox.core.billing.di

import kotlinx.coroutines.Dispatchers
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.BillingClientImpl
import me.matsumo.fanbox.core.billing.BillingClientProvider
import me.matsumo.fanbox.core.billing.BillingClientProviderImpl
import me.matsumo.fanbox.core.billing.BillingStatus
import me.matsumo.fanbox.core.billing.BillingStatusImpl
import me.matsumo.fanbox.core.billing.usecase.ConsumePlusUseCase
import me.matsumo.fanbox.core.billing.usecase.PurchaseDonateUseCase
import me.matsumo.fanbox.core.billing.usecase.PurchasePlusSubscriptionUseCase
import me.matsumo.fanbox.core.billing.usecase.PurchasePlusUseCase
import me.matsumo.fanbox.core.billing.usecase.VerifyPlusUseCase
import org.koin.core.module.Module
import org.koin.dsl.module

actual val billingModule: Module = module {

    single<BillingStatus> {
        BillingStatusImpl(
            userDataRepository = get(),
            billingClient = get(),
            verifyPlusUseCase = get(),
            ioDispatcher = get(),
        )
    }

    single<BillingClient> {
        BillingClientImpl(
            provider = get(),
        )
    }

    single<BillingClientProvider> {
        BillingClientProviderImpl(
            context = get(),
        )
    }

    factory {
        ConsumePlusUseCase(
            billingClient = get(),
        )
    }

    factory {
        PurchaseDonateUseCase(
            billingClient = get(),
            mainDispatcher = Dispatchers.Main,
        )
    }

    factory {
        PurchasePlusSubscriptionUseCase(
            billingClient = get(),
            mainDispatcher = Dispatchers.Main,
        )
    }

    factory {
        PurchasePlusUseCase(
            billingClient = get(),
            mainDispatcher = Dispatchers.Main,
        )
    }

    factory {
        VerifyPlusUseCase(
            billingClient = get(),
        )
    }
}
