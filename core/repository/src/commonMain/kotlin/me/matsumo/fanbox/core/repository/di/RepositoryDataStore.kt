package me.matsumo.fanbox.core.repository.di

import me.matsumo.fanbox.core.repository.UserDataRepositoryImpl
import org.koin.dsl.module

val repositoryDataStoreModule = module {

    single {
        UserDataRepositoryImpl(
            pixiViewDataStore = get(),
        )
    }

    single {

    }
}
