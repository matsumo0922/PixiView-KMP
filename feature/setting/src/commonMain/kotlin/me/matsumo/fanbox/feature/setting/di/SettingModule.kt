package me.matsumo.fanbox.feature.setting.di

import me.matsumo.fanbox.feature.setting.developer.SettingDeveloperViewModel
import me.matsumo.fanbox.feature.setting.directory.SettingDirectoryViewModel
import me.matsumo.fanbox.feature.setting.theme.SettingThemeViewModel
import me.matsumo.fanbox.feature.setting.top.SettingTopViewModel
import me.matsumo.fanbox.feature.setting.translate.SettingTranslationViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val settingModule = module {
    viewModelOf(::SettingTopViewModel)
    viewModelOf(::SettingThemeViewModel)
    viewModelOf(::SettingDeveloperViewModel)
    viewModelOf(::SettingDirectoryViewModel)
    viewModelOf(::SettingTranslationViewModel)
}
