package me.matsumo.fanbox.feature.post.detail

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import app.cash.paging.map
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Creator
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Home
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Search
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Supported
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Unknown
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

class PostDetailRootViewModel(
    private val postId: FanboxPostId,
    private val type: PostDetailPagingType,
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PostDetailRootUiState(
            paging = emptyPaging(),
            userData = UserData.default(),
            bookmarkedPostIds = emptyList(),
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
            userDataRepository.userData.collectLatest {
                _uiState.value = _uiState.value.copy(userData = it)
            }
        }
    }

    fun fetch() {
        viewModelScope.launch {
            val userData = userDataRepository.userData.first()
            val loadSize = if (userData.isHideRestricted || userData.isUseGridMode) 20 else 10
            val isHideRestricted = userData.isHideRestricted

            _uiState.value = PostDetailRootUiState(
                paging = when (type) {
                    Home -> fanboxRepository.getHomePostsPagerCache(loadSize, isHideRestricted).toIdFlow()
                    Supported -> fanboxRepository.getSupportedPostsPagerCache(loadSize, isHideRestricted).toIdFlow()
                    Creator -> fanboxRepository.getCreatorPostsPagerCache()?.toIdFlow()
                    Search -> fanboxRepository.getPostsFromQueryPagerCache()?.toIdFlow()
                    Unknown -> flowOf(PagingData.from(listOf(postId)))
                },
                userData = userData,
                bookmarkedPostIds = fanboxRepository.bookmarkedPostsIds.first(),
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

    private fun Flow<PagingData<FanboxPost>>.toIdFlow(): Flow<PagingData<FanboxPostId>> {
        return map { list -> list.map { it.id } }
    }
}

@Stable
data class PostDetailRootUiState(
    val paging: Flow<PagingData<FanboxPostId>>?,
    val userData: UserData,
    val bookmarkedPostIds: List<FanboxPostId>,
)
