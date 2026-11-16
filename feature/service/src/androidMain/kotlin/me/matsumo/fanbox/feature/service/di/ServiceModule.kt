package me.matsumo.fanbox.feature.service.di

import me.matsumo.fanbox.feature.service.NewPostCheckScheduler
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val serviceModule = module {
    single {
        NewPostCheckScheduler(
            context = androidContext(),
            newPostCheckDataStore = get(),
        )
    }
}
