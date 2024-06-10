package me.matsumo.fanbox.feature.library.di

import me.matsumo.fanbox.feature.library.LibraryViewModel
import me.matsumo.fanbox.feature.library.discovery.LibraryDiscoveryViewModel
import me.matsumo.fanbox.feature.library.home.LibraryHomeViewModel
import me.matsumo.fanbox.feature.library.message.LibraryMessageViewModel
import me.matsumo.fanbox.feature.library.notify.LibraryNotifyViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val libraryModule = module {
    viewModelOf(::LibraryViewModel)
    viewModelOf(::LibraryHomeViewModel)
    viewModelOf(::LibraryNotifyViewModel)
    viewModelOf(::LibraryDiscoveryViewModel)
    viewModelOf(::LibraryMessageViewModel)
}
