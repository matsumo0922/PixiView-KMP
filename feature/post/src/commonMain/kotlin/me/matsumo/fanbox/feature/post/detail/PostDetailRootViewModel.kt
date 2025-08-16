package me.matsumo.fanbox.feature.post.detail

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.PagingData
import app.cash.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Flag
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.FlagRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.ui.customNavTypes
import me.matsumo.fanbox.core.ui.extensition.createStaticPaging
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

class PostDetailRootViewModel(
    savedStateHandle: SavedStateHandle,
    private val settingRepository: SettingRepository,
    private val fanboxRepository: FanboxRepository,
    private val flagRepository: FlagRepository,
) : ViewModel() {

    private val navArgs = savedStateHandle.toRoute<Destination.PostDetail>(customNavTypes)
    private val postId = navArgs.postId
    private val type = navArgs.pagingType

    private val _uiState = MutableStateFlow(
        PostDetailRootUiState(
            paging = emptyPaging(),
            setting = Setting.default(),
            bookmarkedPostIds = emptyList(),
            shouldShowReveal = false,
        ),
    )

    val uiState = _uiState.asStateFlow()

    init {
        fetch()

        viewModelScope.launch {
            fanboxRepository.bookmarkedPostsIds.collectLatest {
                _uiState.value = _uiState.value.copy(bookmarkedPostIds = it)
            }
        }

        viewModelScope.launch {
            settingRepository.setting.collectLatest {
                _uiState.value = _uiState.value.copy(setting = it)
            }
        }
    }

    fun fetch() {
        viewModelScope.launch {
            val userData = settingRepository.setting.first()
            val loadSize = if (userData.isHideRestricted || userData.isUseGridMode) 20 else 10
            val isHideRestricted = userData.isHideRestricted

            val paging = when (type) {
                Destination.PostDetail.PagingType.Home -> fanboxRepository.getHomePostsPagerCache(loadSize, isHideRestricted).toIdFlow()
                Destination.PostDetail.PagingType.Supported -> fanboxRepository.getSupportedPostsPagerCache(loadSize, isHideRestricted).toIdFlow()
                Destination.PostDetail.PagingType.Creator -> fanboxRepository.getCreatorPostsPagerCache()?.toIdFlow()
                Destination.PostDetail.PagingType.Search -> fanboxRepository.getPostsFromQueryPagerCache()?.toIdFlow()
                Destination.PostDetail.PagingType.Unknown -> createStaticPaging(listOf(postId))
            }

            _uiState.value = PostDetailRootUiState(
                paging = paging,
                setting = userData,
                bookmarkedPostIds = fanboxRepository.bookmarkedPostsIds.first(),
                shouldShowReveal = flagRepository.getFlag(Flag.REVEAL_POST_DETAIL, true),
            )
        }
    }

    fun postBookmark(post: FanboxPost, isBookmarked: Boolean) {
        viewModelScope.launch {
            suspendRunCatching {
                if (isBookmarked) {
                    fanboxRepository.bookmarkPost(post)
                } else {
                    fanboxRepository.unbookmarkPost(post)
                }
            }
        }
    }

    fun finishReveal() {
        viewModelScope.launch {
            flagRepository.setFlag(Flag.REVEAL_POST_DETAIL, false)
            _uiState.value = _uiState.value.copy(shouldShowReveal = false)
        }
    }

    private fun Flow<PagingData<FanboxPost>>.toIdFlow(): Flow<PagingData<FanboxPostId>> {
        return map { list -> list.map { it.id } }
    }
}

@Stable
data class PostDetailRootUiState(
    val paging: Flow<PagingData<FanboxPostId>>?,
    val setting: Setting,
    val bookmarkedPostIds: List<FanboxPostId>,
    val shouldShowReveal: Boolean,
)
