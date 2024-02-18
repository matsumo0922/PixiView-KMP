package me.matsumo.fanbox.feature.about.di

import me.matsumo.fanbox.feature.about.billing.BillingPlusViewModel
import me.matsumo.fanbox.feature.about.billing.BillingPlusViewModelImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val aboutSubModule: Module = module {

    factory<BillingPlusViewModel> {
        BillingPlusViewModelImpl(
            userDataRepository = get(),
            ioDispatcher = get()
        )
    }
}
