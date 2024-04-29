package me.matsumo.fanbox.feature.post.search

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.FanboxTag
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class PostSearchViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PostSearchUiState(
            query = "",
            userData = UserData.default(),
            bookmarkedPosts = emptyList(),
            suggestTags = emptyList(),
            creatorPaging = emptyPaging(),
            tagPaging = emptyPaging(),
            postPaging = emptyPaging(),
        ),
    )

    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest { data ->
                _uiState.value = uiState.value.copy(userData = data)
            }
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPosts.collectLatest { data ->
                _uiState.value = uiState.value.copy(bookmarkedPosts = data)
            }
        }
    }

    fun search(query: PostSearchQuery) {
        viewModelScope.launch {
            suspendRunCatching {
                _uiState.value = uiState.value.copy(
                    query = buildQuery(query),
                    userData = userDataRepository.userData.first(),
                )

                when (query.mode) {
                    PostSearchMode.Creator -> {
                        _uiState.value = uiState.value.copy(
                            suggestTags = fanboxRepository.getTagFromQuery(query.creatorQuery.orEmpty()),
                            creatorPaging = fanboxRepository.getCreatorsFromQueryPager(query.creatorQuery.orEmpty()),
                        )
                    }

                    PostSearchMode.Tag -> {
                        _uiState.value = uiState.value.copy(tagPaging = fanboxRepository.getPostsFromQueryPager(query.tag.orEmpty(), query.creatorId))
                    }

                    else -> {
                        _uiState.value = uiState.value.copy(
                            creatorPaging = emptyPaging(),
                            tagPaging = emptyPaging(),
                            postPaging = emptyPaging(),
                        )
                    }
                }
            }.onFailure {
                _uiState.value = uiState.value.copy(
                    creatorPaging = emptyPaging(),
                    tagPaging = emptyPaging(),
                    postPaging = emptyPaging(),
                )
            }
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
data class PostSearchUiState(
    val query: String,
    val userData: UserData,
    val bookmarkedPosts: List<PostId>,
    val suggestTags: List<FanboxTag>,
    val creatorPaging: Flow<PagingData<FanboxCreatorDetail>>,
    val tagPaging: Flow<PagingData<FanboxPost>>,
    val postPaging: Flow<PagingData<FanboxPost>>,
)

enum class PostSearchMode {
    Creator,
    Tag,
    Post,
    Unknown,
}

@Serializable
data class PostSearchQuery(
    val mode: PostSearchMode,
    val creatorId: CreatorId?,
    val creatorQuery: String?,
    val postQuery: String?,
    val tag: String?,
)
