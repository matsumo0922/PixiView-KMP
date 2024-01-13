package me.matsumo.fanbox.feature.creator.support

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlan
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.ui.MR
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

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
                onFailure = { ScreenState.Error(MR.strings.error_network) },
            )
        }
    }
}

@Stable
data class SupportingCreatorsUiState(
    val supportedPlans: List<FanboxCreatorPlan>,
)
