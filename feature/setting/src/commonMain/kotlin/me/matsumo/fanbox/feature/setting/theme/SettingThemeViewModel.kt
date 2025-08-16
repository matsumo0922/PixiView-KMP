package me.matsumo.fanbox.feature.setting.theme

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.repository.SettingRepository

class SettingThemeViewModel(
    private val settingRepository: SettingRepository,
) : ViewModel() {

    val screenState = settingRepository.setting.map {
        ScreenState.Idle(
            SettingThemeState(
                setting = it,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    fun setThemeConfig(themeConfig: ThemeConfig) {
        viewModelScope.launch {
            settingRepository.setThemeConfig(themeConfig)
        }
    }

    fun setThemeColorConfig(themeColorConfig: ThemeColorConfig) {
        viewModelScope.launch {
            settingRepository.setThemeColorConfig(themeColorConfig)
        }
    }

    fun setUseDynamicColor(useDynamicColor: Boolean) {
        viewModelScope.launch {
            settingRepository.setUseDynamicColor(useDynamicColor)
        }
    }
}

@Stable
data class SettingThemeState(
    val setting: Setting,
)
