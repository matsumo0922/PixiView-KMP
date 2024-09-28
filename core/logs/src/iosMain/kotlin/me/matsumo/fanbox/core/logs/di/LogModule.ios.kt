package me.matsumo.fanbox.core.logs.di

import me.matsumo.fanbox.core.logs.logger.LogSender
import me.matsumo.fanbox.core.logs.logger.LogSenderImpl
import org.koin.core.module.Module
import org.koin.core.module.single
import org.koin.dsl.module

actual val logModule: Module = module {
    single<LogSender> {
        LogSenderImpl()
    }
}
