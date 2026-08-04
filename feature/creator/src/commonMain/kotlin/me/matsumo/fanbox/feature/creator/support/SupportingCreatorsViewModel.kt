package me.matsumo.fanbox.feature.creator.support

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.toScreenStateError
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlan

class SupportingCreatorsViewModel(
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<SupportingCreatorsUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    init {
        fetch()
    }

    fun fetch() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                SupportingCreatorsUiState(
                    supportedPlans = fanboxRepository.getSupportedPlans(),
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { it.toScreenStateError() },
            )
        }
    }
}

@Stable
data class SupportingCreatorsUiState(
    val supportedPlans: List<FanboxCreatorPlan>,
)
