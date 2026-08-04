package me.matsumo.fanbox.core.datastore.di

import me.matsumo.fanbox.core.datastore.DummyDataStore
import me.matsumo.fanbox.core.datastore.DummyDataStoreImpl
import me.matsumo.fanbox.core.datastore.PreferenceHelper
import me.matsumo.fanbox.core.datastore.PreferenceHelperImpl
import me.matsumo.fanbox.core.datastore.cookie.MigratingFanboxCookieStorage
import me.matsumo.fanbox.core.datastore.cookie.createCookieStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val dataStoreHelperModule: Module = module {

    // アプリの生存期間に一致する単一の実体とする。同じ保存先に対して複数の実体を開くと
    // 状態が食い違う。閉じる時点が存在しないため close は行わない。
    single<MigratingFanboxCookieStorage> {
        createCookieStorage(
            context = get(),
            ioDispatcher = get(),
        )
    }

    single<PreferenceHelper> {
        PreferenceHelperImpl(
            context = get(),
            ioDispatcher = get(),
        )
    }

    single<DummyDataStore> {
        DummyDataStoreImpl(
            context = get(),
            userDataStore = get(),
        )
    }
}
