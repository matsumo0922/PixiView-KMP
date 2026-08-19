package me.matsumo.fanbox.feature.library.message

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.NewsLetter
import me.matsumo.fanbox.core.model.toScreenStateError
import me.matsumo.fanbox.core.repository.FanboxRepository

class LibraryMessageViewModel(
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<LibraryMessageUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    init {
        fetch()
    }

    fun fetch() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                LibraryMessageUiState(
                    messages = fanboxRepository.getNewsLetters(),
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { it.toScreenStateError() },
            )
        }
    }
}

@Stable
data class LibraryMessageUiState(
    val messages: List<NewsLetter>,
)
