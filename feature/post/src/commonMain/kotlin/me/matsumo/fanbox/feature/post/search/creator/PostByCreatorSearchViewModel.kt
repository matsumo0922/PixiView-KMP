package me.matsumo.fanbox.feature.post.search.creator

import androidx.compose.runtime.Stable
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.ui.customNavTypes
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId

class PostByCreatorSearchViewModel(
    savedStateHandle: SavedStateHandle,
    private val settingRepository: SettingRepository,
    private val fanboxRepository: FanboxRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO,
) : ViewModel() {

    private val creatorId = savedStateHandle.toRoute<Destination.PostByCreatorSearch>(customNavTypes).creatorId

    private val _allPosts = MutableSharedFlow<List<FanboxPost>>(replay = 1)
    private val _query = MutableStateFlow("")
    private val _screenState = MutableStateFlow<ScreenState<PostByCreatorSearchUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()
    val query = _query.asStateFlow()

    init {
        fetch()

        viewModelScope.launch {
            query.collectLatest { query ->
                val searchedPosts = if (query.isBlank()) emptyList() else searchPosts(query)
                _screenState.updateWhenIdle { it.copy(searchedPosts = searchedPosts) }
            }
        }
    }

    fun fetch() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                PostByCreatorSearchUiState(
                    creatorId = creatorId,
                    creatorDetail = fanboxRepository.getCreatorDetail(creatorId),
                    setting = settingRepository.setting.first(),
                    bookmarkedPostsIds = fanboxRepository.bookmarkedPostsIds.first(),
                    searchedPosts = emptyList(),
                    progress = 0f,
                    isPrepared = false,
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { ScreenState.Error(Res.string.error_network) },
            )

            fetchPosts()
        }
    }

    private suspend fun fetchPosts() {
        fanboxRepository.getCreatorAllPostsCache(creatorId)?.let { cachedPosts ->
            _allPosts.emit(cachedPosts)
            _screenState.updateWhenIdle { it.copy(progress = 1f, isPrepared = true) }
            return
        }

        val posts = mutableListOf<FanboxPost>()
        val paginate = withContext(ioDispatcher) { fanboxRepository.getCreatorPostsPagination(creatorId) }
        val max = paginate.sumOf { it.limit ?: 10 }

        for (cursor in paginate) {
            posts.addAll(
                withContext(ioDispatcher) {
                    fanboxRepository.getCreatorPosts(creatorId, cursor, null).contents
                },
            )

            _screenState.updateWhenIdle { it.copy(progress = posts.size.toFloat() / max) }
        }

        val data = posts.distinctBy { post -> post.id }

        fanboxRepository.setCreatorAllPostsCache(creatorId, data)

        _allPosts.emit(data)
        _screenState.updateWhenIdle {
            it.copy(
                progress = 1f,
                isPrepared = true,
            )
        }
    }

    private suspend fun searchPosts(query: String): List<FanboxPost> {
        return _allPosts.first().filter { post ->
            val title = post.title.contains(query)
            val excerpt = post.excerpt.contains(query)
            val id = post.id.toString().contains(query)

            title || excerpt || id
        }
    }

    fun updateQuery(query: String) {
        _query.value = query
    }

    suspend fun follow(creatorUserId: FanboxUserId): Result<Unit> {
        return suspendRunCatching {
            fanboxRepository.unfollowCreator(creatorUserId)
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
data class PostByCreatorSearchUiState(
    val creatorId: FanboxCreatorId,
    val creatorDetail: FanboxCreatorDetail,
    val setting: Setting,
    val bookmarkedPostsIds: List<FanboxPostId>,
    val searchedPosts: List<FanboxPost>,
    val progress: Float,
    val isPrepared: Boolean,
)
