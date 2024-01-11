package me.matsumo.fanbox.feature.setting.theme

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.repository.UserDataRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope


class SettingThemeViewModel(
    private val userDataRepository: UserDataRepository,
) : ViewModel() {

    val screenState = userDataRepository.userData.map {
        ScreenState.Idle(
            SettingThemeState(
                userData = it,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    fun setThemeConfig(themeConfig: ThemeConfig) {
        viewModelScope.launch {
            userDataRepository.setThemeConfig(themeConfig)
        }
    }

    fun setThemeColorConfig(themeColorConfig: ThemeColorConfig) {
        viewModelScope.launch {
            userDataRepository.setThemeColorConfig(themeColorConfig)
        }
    }

    fun setUseDynamicColor(useDynamicColor: Boolean) {
        viewModelScope.launch {
            userDataRepository.setUseDynamicColor(useDynamicColor)
        }
    }
}

@Stable
data class SettingThemeState(
    val userData: UserData,
)
