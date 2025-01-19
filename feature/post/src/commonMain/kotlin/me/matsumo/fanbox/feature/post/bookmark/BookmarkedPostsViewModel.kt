package me.matsumo.fanbox.feature.post.bookmark

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

class BookmarkedPostsViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<LikedPostsUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            fanboxRepository.bookmarkedPostsIds.collectLatest {
                _screenState.updateWhenIdle { data ->
                    data.copy(bookmarkedPostIds = it)
                }
            }
        }
    }

    fun fetch() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = ScreenState.Idle(
                LikedPostsUiState(
                    userData = userDataRepository.userData.first(),
                    posts = fanboxRepository.getBookmarkedPosts(),
                    bookmarkedPostIds = fanboxRepository.bookmarkedPostsIds.first(),
                ),
            )
        }
    }

    fun search(query: String) {
        viewModelScope.launch {
            val posts = fanboxRepository.getBookmarkedPosts()
            val result = if (query.isBlank()) {
                posts
            } else {
                posts.filter { post ->
                    val isMatchTitle = post.title.contains(query, ignoreCase = true)
                    val isMatchBody = post.excerpt.contains(query, ignoreCase = true)
                    val isMatchTag = post.tags.any { tag -> tag.contains(query, ignoreCase = true) }
                    val isMatchCreatorName = post.user?.name.orEmpty().contains(query, ignoreCase = true)
                    val isMatchFanboxCreatorId = post.user?.creatorId?.value.orEmpty().contains(query, ignoreCase = true)

                    isMatchTitle || isMatchBody || isMatchTag || isMatchCreatorName || isMatchFanboxCreatorId
                }
            }

            _screenState.updateWhenIdle { it.copy(posts = result) }
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

@Stable
data class LikedPostsUiState(
    val userData: UserData,
    val posts: List<FanboxPost>,
    val bookmarkedPostIds: List<FanboxPostId>,
)
