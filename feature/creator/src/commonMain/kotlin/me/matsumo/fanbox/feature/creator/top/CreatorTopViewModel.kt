package me.matsumo.fanbox.feature.creator.top

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlan
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorTag
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.RewardRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.error_network

class CreatorTopViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val rewardRepository: RewardRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<CreatorTopUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    private var postsPagingCache: Flow<PagingData<FanboxPost>>? = null

    init {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest { data ->
                _screenState.value = screenState.updateWhenIdle { it.copy(userData = data) }
            }
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPosts.collectLatest { data ->
                _screenState.value = screenState.updateWhenIdle { it.copy(bookmarkedPosts = data) }
            }
        }

        viewModelScope.launch {
            fanboxRepository.blockedCreators.collectLatest { _ ->
                _screenState.value = ScreenState.Loading
            }
        }
    }

    fun fetch(creatorId: CreatorId) {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                val userData = userDataRepository.userData.first()
                val loadSize = if (userData.isHideRestricted || userData.isUseGridMode) 20 else 10

                CreatorTopUiState(
                    userData = userData,
                    bookmarkedPosts = fanboxRepository.bookmarkedPosts.first(),
                    isBlocked = fanboxRepository.blockedCreators.first().contains(creatorId),
                    isAbleToReward = rewardRepository.isAbleToReward(),
                    creatorDetail = fanboxRepository.getCreator(creatorId),
                    creatorPlans = fanboxRepository.getCreatorPlans(creatorId),
                    creatorTags = fanboxRepository.getCreatorTags(creatorId),
                    creatorPostsPaging = postsPagingCache ?: fanboxRepository.getCreatorPostsPager(creatorId, loadSize).also {
                        postsPagingCache = it
                    },
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { ScreenState.Error(Res.string.error_network) },
            )
        }
    }

    suspend fun follow(creatorUserId: String): Result<Unit> {
        return suspendRunCatching {
            fanboxRepository.followCreator(creatorUserId)
        }
    }

    suspend fun unfollow(creatorUserId: String): Result<Unit> {
        return suspendRunCatching {
            fanboxRepository.unfollowCreator(creatorUserId)
        }
    }

    fun postLike(postId: PostId) {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.likePost(postId)
            }
        }
    }

    fun postBookmark(post: FanboxPost, isBookmarked: Boolean) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also {
                if (isBookmarked) {
                    fanboxRepository.bookmarkPost(post)
                } else {
                    fanboxRepository.unbookmarkPost(post)
                }
            }
        }
    }

    suspend fun blockCreator(creatorId: CreatorId) {
        suspendRunCatching {
            fanboxRepository.blockCreator(creatorId)
        }
    }

    suspend fun unblockCreator(creatorId: CreatorId) {
        suspendRunCatching {
            fanboxRepository.unblockCreator(creatorId)
        }
    }

    fun rewarded() {
        viewModelScope.launch {
            rewardRepository.rewarded()
        }
    }
}

@Stable
data class CreatorTopUiState(
    val userData: UserData,
    val bookmarkedPosts: List<PostId>,
    val creatorDetail: FanboxCreatorDetail,
    val creatorPlans: List<FanboxCreatorPlan>,
    val creatorTags: List<FanboxCreatorTag>,
    val creatorPostsPaging: Flow<PagingData<FanboxPost>>,
    val isBlocked: Boolean,
    val isAbleToReward: Boolean,
)
