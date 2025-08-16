package me.matsumo.fanbox.core.billing.di

import me.matsumo.fanbox.core.billing.BillingStatus
import me.matsumo.fanbox.core.billing.BillingStatusImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val billingModule: Module = module {
    single<BillingStatus> {
        BillingStatusImpl(
            settingRepository = get(),
            ioDispatcher = get(),
        )
    }
}
