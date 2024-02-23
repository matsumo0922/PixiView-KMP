package me.matsumo.fanbox.feature.library.di

import me.matsumo.fanbox.feature.library.LibraryViewModel
import me.matsumo.fanbox.feature.library.discovery.LibraryDiscoveryViewModel
import me.matsumo.fanbox.feature.library.home.LibraryHomeViewModel
import me.matsumo.fanbox.feature.library.message.LibraryMessageViewModel
import me.matsumo.fanbox.feature.library.notify.LibraryNotifyViewModel
import org.koin.dsl.module

val libraryModule = module {

    factory {
        LibraryViewModel(
            userDataRepository = get(),
        )
    }

    factory {
        LibraryHomeViewModel(
            userDataRepository = get(),
            fanboxRepository = get(),
        )
    }

    factory {
        LibraryNotifyViewModel(
            fanboxRepository = get(),
        )
    }

    factory {
        LibraryDiscoveryViewModel(
            fanboxRepository = get(),
        )
    }

    factory {
        LibraryMessageViewModel(
            fanboxRepository = get(),
        )
    }
}
