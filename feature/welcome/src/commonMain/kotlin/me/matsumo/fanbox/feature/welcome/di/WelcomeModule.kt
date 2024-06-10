package me.matsumo.fanbox.feature.welcome.di

import me.matsumo.fanbox.feature.welcome.login.WelcomeLoginViewModel
import me.matsumo.fanbox.feature.welcome.top.WelcomeTopViewModel
import me.matsumo.fanbox.feature.welcome.web.WelcomeWebViewModel
import org.koin.compose.viewmodel.dsl.viewModelOf
import org.koin.dsl.module

val welcomeModule = module {
    viewModelOf(::WelcomeTopViewModel)
    viewModelOf(::WelcomeLoginViewModel)
    viewModelOf(::WelcomeWebViewModel)
}
