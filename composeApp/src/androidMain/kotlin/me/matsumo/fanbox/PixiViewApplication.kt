package me.matsumo.fanbox

import android.app.Application
import me.matsumo.fanbox.core.datastore.di.dataStoreHelperModule
import me.matsumo.fanbox.core.datastore.di.dataStoreModule
import me.matsumo.fanbox.di.appModule
import me.matsumo.fanbox.di.initKoin
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import org.koin.dsl.module

class PixiViewApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initKoin {
            androidContext(applicationContext)
            androidLogger()
        }
    }
}
