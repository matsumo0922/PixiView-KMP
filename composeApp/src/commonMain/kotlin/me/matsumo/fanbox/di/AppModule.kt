package me.matsumo.fanbox.di

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import me.matsumo.fanbox.BuildKonfig
import me.matsumo.fanbox.PixiViewViewModel
import me.matsumo.fanbox.core.common.PixiViewConfig
import org.koin.compose.viewmodel.dsl.viewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

expect fun getPixiViewConfig(): PixiViewConfig

@OptIn(ExperimentalCoroutinesApi::class)
val appModule = module {

    single {
        getPixiViewConfig()
    }

    single<CoroutineDispatcher> {
        Dispatchers.IO.limitedParallelism(24)
    }

    single<CoroutineScope> {
        CoroutineScope(SupervisorJob() + get<CoroutineDispatcher>())
    }

    viewModelOf(::PixiViewViewModel)
}
