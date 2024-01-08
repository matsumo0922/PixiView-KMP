package me.matsumo.fanbox.feature.welcome.di

import me.matsumo.fanbox.feature.welcome.top.WelcomeTopViewModel
import moe.tlaster.precompose.viewmodel.viewModel
import org.koin.dsl.module

val welcomeModule = module {

    factory {
        WelcomeTopViewModel(
            userDataRepository = get(),
        )
    }
}
