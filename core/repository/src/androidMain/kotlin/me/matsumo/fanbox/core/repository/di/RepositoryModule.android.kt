package me.matsumo.fanbox.core.repository.di

import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.DownloadPostsRepositoryImpl
import me.matsumo.fankt.fanbox.FanboxCookieStorage
import me.matsumo.fankt.fanbox.persistence.room.createRoomFanboxCookieStorage
import org.koin.core.module.Module
import org.koin.dsl.module

actual val repositorySubModule: Module = module {
    // 既存の fankt.db を保存先として引き継ぐ。注入しないと fankt 0.1.0 は Cookie をメモリ上にしか
    // 持たず、プロセスが作り直されるたびにログアウトする。
    //
    // single にすることでアプリの生存期間に一致する単一インスタンスになる。同一の fankt.db に
    // 対して複数の storage を開くと Room の invalidation tracker が伝播せず cookies Flow が
    // 更新されない。close は行わない。プロセス終了まで生きるため呼ぶ時点が存在せず、storage 自身の
    // ioDispatcher 上から close を呼ぶと Room の close バリアでデッドロックしうる。
    single<FanboxCookieStorage> {
        createRoomFanboxCookieStorage(
            context = get(),
            ioDispatcher = get(),
        )
    }

    single<DownloadPostsRepository> {
        DownloadPostsRepositoryImpl(
            context = get(),
            userDataStore = get(),
            fanboxRepository = get(),
            scope = get(),
        )
    }
}
