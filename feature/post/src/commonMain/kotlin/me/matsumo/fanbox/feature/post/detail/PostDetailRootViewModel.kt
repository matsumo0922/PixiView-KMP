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
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.extensition.createStaticPaging
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

class PostDetailRootViewModel(
    savedStateHandle: SavedStateHandle,
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val postId = savedStateHandle.toRoute<Destination.PostDetail>().postId
    private val type = savedStateHandle.toRoute<Destination.PostDetail>().pagingType

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

            val paging = when (type) {
                Destination.PostDetail.PagingType.Home -> fanboxRepository.getHomePostsPagerCache(loadSize, isHideRestricted).toIdFlow()
                Destination.PostDetail.PagingType.Supported -> fanboxRepository.getSupportedPostsPagerCache(loadSize, isHideRestricted).toIdFlow()
                Destination.PostDetail.PagingType.Creator -> fanboxRepository.getCreatorPostsPagerCache()?.toIdFlow()
                Destination.PostDetail.PagingType.Search -> fanboxRepository.getPostsFromQueryPagerCache()?.toIdFlow()
                Destination.PostDetail.PagingType.Unknown -> createStaticPaging(listOf(postId))
            }

            _uiState.value = PostDetailRootUiState(
                paging = paging,
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
