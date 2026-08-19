package me.matsumo.fanbox.feature.creator.follow

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail
import me.matsumo.fanbox.core.model.fanbox.UserId
import me.matsumo.fanbox.core.model.toScreenStateError
import me.matsumo.fanbox.core.repository.FanboxRepository

class FollowingCreatorsViewModel(
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<FollowingCreatorsUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    init {
        fetch()
    }

    fun fetch() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                FollowingCreatorsUiState(
                    followingCreators = fanboxRepository.getFollowingCreators(),
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { it.toScreenStateError() },
            )
        }
    }

    suspend fun follow(creatorUserId: UserId): Result<Unit> {
        return suspendRunCatching {
            fanboxRepository.followCreator(creatorUserId)
        }
    }

    suspend fun unfollow(creatorUserId: UserId): Result<Unit> {
        return suspendRunCatching {
            fanboxRepository.unfollowCreator(creatorUserId)
        }
    }
}

@Stable
data class FollowingCreatorsUiState(
    val followingCreators: List<CreatorDetail>,
)
