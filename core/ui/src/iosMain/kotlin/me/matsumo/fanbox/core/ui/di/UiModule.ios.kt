package me.matsumo.fanbox.core.ui.di

import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtensionImpl
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.ToastExtensionImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val uiSubModule: Module = module {

    single<NavigatorExtension> {
        NavigatorExtensionImpl()
    }

    single<ToastExtension> {
        ToastExtensionImpl()
    }
}
