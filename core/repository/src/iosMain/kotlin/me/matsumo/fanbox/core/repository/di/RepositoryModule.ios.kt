package me.matsumo.fanbox.core.repository.di

import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.DownloadPostsRepositoryImpl
import org.koin.core.module.Module
import org.koin.dsl.module

actual val repositorySubModule: Module = module {
    single<DownloadPostsRepository> {
        DownloadPostsRepositoryImpl(
            fanboxRepository = get(),
            userDataStore = get(),
            scope = get(),
        )
    }
}
