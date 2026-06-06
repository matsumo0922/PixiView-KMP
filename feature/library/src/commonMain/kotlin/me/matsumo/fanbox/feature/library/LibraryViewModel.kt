package me.matsumo.fanbox.feature.library

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.repository.SettingRepository

class LibraryViewModel(
    settingRepository: SettingRepository,
) : ViewModel() {

    val screenState = settingRepository.setting.map {
        ScreenState.Idle(
            LibraryUiState(
                setting = it,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )
}

@Stable
data class LibraryUiState(
    val setting: Setting,
)
