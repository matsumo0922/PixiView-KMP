package me.matsumo.fanbox.core.ui.di

import me.matsumo.fanbox.core.ui.ads.NativeAdsPreLoader
import me.matsumo.fanbox.core.ui.ads.RewardAdLoader
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtensionImpl
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.ToastExtensionImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val uiSubModule: Module = module {

    single<NavigatorExtension> {
        NavigatorExtensionImpl(
            context = get(),
        )
    }

    single<ToastExtension> {
        ToastExtensionImpl(
            context = get(),
        )
    }

    single<NativeAdsPreLoader> {
        NativeAdsPreLoader(
            context = get(),
            pixiViewConfig = get(),
            ioDispatcher = get(),
        )
    }

    single<RewardAdLoader> {
        RewardAdLoader(
            context = get(),
            pixiViewConfig = get(),
        )
    }
}
