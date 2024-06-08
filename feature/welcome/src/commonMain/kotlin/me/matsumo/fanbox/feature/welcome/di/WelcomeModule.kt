package me.matsumo.fanbox.feature.welcome.di

import me.matsumo.fanbox.feature.welcome.login.WelcomeLoginViewModel
import me.matsumo.fanbox.feature.welcome.top.WelcomeTopViewModel
import me.matsumo.fanbox.feature.welcome.web.WelcomeWebViewModel
import org.koin.dsl.module

val welcomeModule = module {

    factory {
        WelcomeTopViewModel(
            userDataRepository = get(),
        )
    }

    factory {
        WelcomeLoginViewModel(
            userDataRepository = get(),
            fanboxRepository = get(),
        )
    }

    factory {
        WelcomeWebViewModel(
            fanboxRepository = get(),
        )
    }
}
