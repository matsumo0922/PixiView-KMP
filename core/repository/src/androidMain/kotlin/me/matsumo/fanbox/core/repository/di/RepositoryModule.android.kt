package me.matsumo.fanbox.core.repository.di

import android.content.Context
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.DownloadPostsRepositoryImpl
import me.matsumo.fankt.fanbox.FanboxGuestCache
import org.koin.core.module.Module
import org.koin.dsl.module
import java.io.File

actual val repositorySubModule: Module = module {
    single<DownloadPostsRepository> {
        DownloadPostsRepositoryImpl(
            context = get(),
            userDataStore = get(),
            fanboxRepository = get(),
            scope = get(),
        )
    }

    // guest bundle は配信先から取得し直せるため、OS が消せる cacheDir に置く。
    single {
        val context = get<Context>()

        FanboxGuestCache(context, File(context.cacheDir, "fanbox-guest"))
    }
}
