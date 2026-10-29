package me.matsumo.fanbox.feature.about.about

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.repository.SettingRepository
import kotlin.time.Duration.Companion.seconds

class AboutViewModel(pixiViewConfig: PixiViewConfig, settingRepository: SettingRepository) : ViewModel() {

    val screenState = settingRepository.setting.map {
        ScreenState.Idle(
            AboutUiState(
                setting = it,
                config = pixiViewConfig,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
        initialValue = ScreenState.Loading,
    )
}

@Stable
data class AboutUiState(val setting: Setting, val config: PixiViewConfig)
