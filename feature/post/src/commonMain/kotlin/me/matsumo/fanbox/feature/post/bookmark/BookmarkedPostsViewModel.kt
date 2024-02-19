package me.matsumo.fanbox.feature.post.bookmark

import androidx.compose.runtime.Stable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class BookmarkedPostsViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<LikedPostsUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            fanboxRepository.bookmarkedPosts.collectLatest { bookmarkedPosts ->
                _screenState.value = screenState.updateWhenIdle { data ->
                    data.copy(bookmarkedPosts = data.bookmarkedPosts.map { it.copy(isBookmarked = it.id in bookmarkedPosts) })
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
                    bookmarkedPosts = fanboxRepository.getBookmarkedPosts(),
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
                    val isMatchCreatorName = post.user.name.contains(query, ignoreCase = true)
                    val isMatchCreatorId = post.user.creatorId.value.contains(query, ignoreCase = true)

                    isMatchTitle || isMatchBody || isMatchTag || isMatchCreatorName || isMatchCreatorId
                }
            }

            _screenState.value = screenState.updateWhenIdle { it.copy(bookmarkedPosts = result) }
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
    val bookmarkedPosts: List<FanboxPost>,
)
