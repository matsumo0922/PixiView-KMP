package me.matsumo.fanbox.core.ui.di

import me.matsumo.fanbox.core.ui.ads.NativeAdsPreLoader
import me.matsumo.fanbox.core.ui.extensition.ImageDownloader
import me.matsumo.fanbox.core.ui.extensition.ImageDownloaderImpl
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtensionImpl
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtension
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtensionImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val uiSubModule: Module = module {

    single<NavigatorExtension> {
        NavigatorExtensionImpl(
            context = get(),
        )
    }

    single<SnackbarExtension> {
        SnackbarExtensionImpl(
            context = get(),
        )
    }

    single<ImageDownloader> {
        ImageDownloaderImpl(
            context = get(),
            fanboxRepository = get(),
        )
    }

    single<NativeAdsPreLoader> {
        NativeAdsPreLoader(
            context = get(),
            pixiViewConfig = get(),
            ioDispatcher = get(),
        )
    }
}
