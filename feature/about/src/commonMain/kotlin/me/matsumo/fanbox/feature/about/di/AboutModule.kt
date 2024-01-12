package me.matsumo.fanbox.feature.about.di

import me.matsumo.fanbox.feature.about.about.AboutViewModel
import me.matsumo.fanbox.feature.about.billing.BillingPlusViewModel
import org.koin.dsl.module

val aboutModule = module {

    factory {
        AboutViewModel(
            pixiViewConfig = get(),
            userDataRepository = get(),
        )
    }

    factory {
        BillingPlusViewModel(
            userDataRepository = get(),
            ioDispatcher = get(),
        )
    }
}
