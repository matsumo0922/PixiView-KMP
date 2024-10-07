package me.matsumo.fanbox.feature.setting.directory

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.repository.UserDataRepository

class SettingDirectoryViewModel(
    private val userDataRepository: UserDataRepository,
): ViewModel() {

    val screenState = userDataRepository.userData.map {
        ScreenState.Idle(
            SettingDirectoryUiState(
                imageDirectory = it.imageSaveDirectory,
                fileDirectory = it.fileSaveDirectory,
                postDirectory = it.postSaveDirectory,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    fun setImageSaveDirectory(directory: String) {
        viewModelScope.launch {
            userDataRepository.setImageSaveDirectory(directory)
        }
    }

    fun setFileSaveDirectory(directory: String) {
        viewModelScope.launch {
            userDataRepository.setFileSaveDirectory(directory)
        }
    }

    fun setPostSaveDirectory(directory: String) {
        viewModelScope.launch {
            userDataRepository.setPostSaveDirectory(directory)
        }
    }
}

@Stable
data class SettingDirectoryUiState(
    val imageDirectory: String,
    val fileDirectory: String,
    val postDirectory: String,
)
