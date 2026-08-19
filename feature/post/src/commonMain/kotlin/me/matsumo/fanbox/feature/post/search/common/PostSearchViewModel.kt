package me.matsumo.fanbox.feature.post.search.common

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
import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.Tag
import me.matsumo.fanbox.core.model.fanbox.UserId
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.ui.extensition.emptyPaging

class PostSearchViewModel(
    private val settingRepository: SettingRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PostSearchUiState(
            query = "",
            setting = Setting.default(),
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
            settingRepository.setting.collectLatest { data ->
                _uiState.value = uiState.value.copy(setting = data)
            }
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPostsIds.collectLatest { data ->
                _uiState.value = uiState.value.copy(bookmarkedPosts = data)
            }
        }
    }

    fun search(query: PostSearchQuery) {
        viewModelScope.launch {
            suspendRunCatching {
                _uiState.value = uiState.value.copy(
                    query = buildQuery(query),
                    setting = settingRepository.setting.first(),
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

    fun postLike(postId: PostId) {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.likePost(postId)
            }
        }
    }

    fun postBookmark(post: Post, isBookmarked: Boolean) {
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
    val setting: Setting,
    val bookmarkedPosts: List<PostId>,
    val suggestTags: List<Tag>,
    val creatorPaging: Flow<PagingData<CreatorDetail>>,
    val tagPaging: Flow<PagingData<Post>>,
    val postPaging: Flow<PagingData<Post>>,
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
