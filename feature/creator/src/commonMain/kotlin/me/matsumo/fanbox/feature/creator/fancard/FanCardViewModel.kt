package me.matsumo.fanbox.feature.creator.fancard

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlanDetail
import me.matsumo.fanbox.core.model.fanbox.id.FanboxCreatorId
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_back
import me.matsumo.fanbox.core.resources.creator_fan_card_not_supported

class FanCardViewModel(
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<FanCardUiState>>(ScreenState.Loading)
    private val _downloadedEvent = Channel<Boolean>()

    val screenState = _screenState.asStateFlow()
    val downloadedEvent = _downloadedEvent.receiveAsFlow()

    fun fetch(creatorId: FanboxCreatorId) {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                FanCardUiState(
                    planDetail = fanboxRepository.getCreatorPlan(creatorId),
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = {
                    ScreenState.Error(
                        message = Res.string.creator_fan_card_not_supported,
                        retryTitle = Res.string.common_back,
                    )
                },
            )
        }
    }
}

@Stable
data class FanCardUiState(
    val planDetail: FanboxCreatorPlanDetail,
)
