package me.matsumo.fanbox.feature.creator.top

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Flag
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.TranslationState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.FlagRepository
import me.matsumo.fanbox.core.repository.RewardRepository
import me.matsumo.fanbox.core.repository.TranslationRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.ui.customNavTypes
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlan
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxTag
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.jetbrains.compose.resources.getString

class CreatorTopViewModel(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val translationRepository: TranslationRepository,
    private val rewardRepository: RewardRepository,
    private val flagRepository: FlagRepository,
) : ViewModel() {

    private val creatorId = savedStateHandle.toRoute<Destination.CreatorTop>(customNavTypes).creatorId
    private var postsPagingCache: Flow<PagingData<FanboxPost>>? = null

    private val _screenState = MutableStateFlow<ScreenState<CreatorTopUiState>>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        fetch()

        viewModelScope.launch {
            userDataRepository.userData.collectLatest { data ->
                _screenState.updateWhenIdle { it.copy(userData = data) }
            }
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPostsIds.collectLatest { data ->
                _screenState.updateWhenIdle { it.copy(bookmarkedPostsIds = data) }
            }
        }

        viewModelScope.launch {
            fanboxRepository.blockedCreators.collectLatest { _ ->
                _screenState.value = ScreenState.Loading
            }
        }
    }

    fun fetch() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                val userData = userDataRepository.userData.first()
                val loadSize = if (userData.isHideRestricted || userData.isUseGridMode) 20 else 10

                CreatorTopUiState(
                    userData = userData,
                    bookmarkedPostsIds = fanboxRepository.bookmarkedPostsIds.first(),
                    isBlocked = fanboxRepository.blockedCreators.first().contains(creatorId),
                    isAbleToReward = rewardRepository.isAbleToReward(),
                    shouldShowReveal = flagRepository.getFlag(Flag.REVEAL_CREATOR_TOP, true),
                    creatorDetail = fanboxRepository.getCreatorDetail(creatorId),
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

    fun finishReveal() {
        viewModelScope.launch {
            flagRepository.setFlag(Flag.REVEAL_CREATOR_TOP, false)

            _screenState.updateWhenIdle {
                it.copy(shouldShowReveal = false)
            }
        }
    }

    suspend fun follow(creatorUserId: FanboxUserId): Result<Unit> {
        return suspendRunCatching {
            fanboxRepository.followCreator(creatorUserId)
        }
    }

    suspend fun unfollow(creatorUserId: FanboxUserId): Result<Unit> {
        return suspendRunCatching {
            fanboxRepository.unfollowCreator(creatorUserId)
        }
    }

    fun postLike(postId: FanboxPostId) {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.likePost(postId)
            }
        }
    }

    fun postBookmark(post: FanboxPost, isBookmarked: Boolean) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also {
                suspendRunCatching {
                    if (isBookmarked) {
                        fanboxRepository.bookmarkPost(post)
                    } else {
                        fanboxRepository.unbookmarkPost(post)
                    }
                }
            }
        }
    }

    suspend fun blockCreator() {
        suspendRunCatching {
            fanboxRepository.blockCreator(creatorId)
        }
    }

    suspend fun unblockCreator() {
        suspendRunCatching {
            fanboxRepository.unblockCreator(creatorId)
        }
    }

    fun translateDescription(description: String) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { state ->
                if (state.data.descriptionTransState is TranslationState.Translated) return@launch

                _screenState.updateWhenIdle { it.copy(descriptionTransState = TranslationState.Loading) }

                val translatedDescription = suspendRunCatching {
                    translationRepository.translate(description, Locale("en"))
                }.fold(
                    onSuccess = { it },
                    onFailure = { getString(Res.string.error_network) }
                )

                _screenState.updateWhenIdle {
                    it.copy(
                        descriptionTransState = TranslationState.Translated(translatedDescription),
                        creatorDetail = it.creatorDetail.copy(
                            description = translatedDescription
                        )
                    )
                }
            }
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
    val bookmarkedPostsIds: List<FanboxPostId>,
    val creatorDetail: FanboxCreatorDetail,
    val creatorPlans: List<FanboxCreatorPlan>,
    val creatorTags: List<FanboxTag>,
    val creatorPostsPaging: Flow<PagingData<FanboxPost>>,
    val isBlocked: Boolean,
    val isAbleToReward: Boolean,
    val shouldShowReveal: Boolean,
    val descriptionTransState: TranslationState<String> = TranslationState.None,
)
