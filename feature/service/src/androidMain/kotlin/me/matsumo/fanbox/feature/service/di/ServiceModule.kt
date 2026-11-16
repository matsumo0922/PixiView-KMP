package me.matsumo.fanbox.feature.service.di

import me.matsumo.fanbox.feature.service.NewContentCheckScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single {
        NewContentCheckScheduler(
            context = androidContext(),
            newContentCheckDataStore = get(),
        )
    }
}
