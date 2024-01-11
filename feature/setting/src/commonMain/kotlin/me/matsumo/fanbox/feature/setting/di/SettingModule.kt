package me.matsumo.fanbox.feature.setting.di

import me.matsumo.fanbox.feature.setting.developer.SettingDeveloperViewModel
import me.matsumo.fanbox.feature.setting.theme.SettingThemeViewModel
import me.matsumo.fanbox.feature.setting.top.SettingTopViewModel
import org.koin.dsl.module

val settingModule = module {

    factory {
        SettingTopViewModel(
            pixiViewConfig = get(),
            userDataRepository = get(),
            fanboxRepository = get(),
        )
    }

    factory {
        SettingThemeViewModel(
            userDataRepository = get(),
        )
    }

    factory {
        SettingDeveloperViewModel(
            pixiViewConfig = get(),
            userDataRepository = get(),
        )
    }
}
