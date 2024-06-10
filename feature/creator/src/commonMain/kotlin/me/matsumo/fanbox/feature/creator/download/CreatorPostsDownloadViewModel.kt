package me.matsumo.fanbox.feature.creator.download

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxCursor
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.extensition.ImageDownloader
import kotlin.coroutines.resume

class CreatorPostsDownloadViewModel(
    private val fanboxRepository: FanboxRepository,
    private val imageDownloader: ImageDownloader,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<CreatorPostsDownloadUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    fun fetch(creatorId: CreatorId) {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                val creatorDetail = fanboxRepository.getCreator(creatorId)
                val paginate = fanboxRepository.getCreatorPostsPaginate(creatorId)

                CreatorPostsDownloadUiState(
                    creatorDetail = creatorDetail,
                    postsPaginate = paginate,
                    posts = mutableListOf(),
                    targetPosts = mutableListOf(),
                    ignoreKeyword = "",
                    isIgnoreFreePosts = false,
                    isIgnoreFiles = false,
                    isPrepared = false,
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { ScreenState.Error(MR.strings.error_network) },
            )
        }
    }

    suspend fun fetchPosts(
        creatorId: CreatorId,
        paginate: List<FanboxCursor>,
        updateCallback: (Float) -> Unit,
    ) {
        val max = paginate.sumOf { it.limit ?: 10 }
        val posts = mutableListOf<FanboxPost>()

        for (cursor in paginate) {
            posts.addAll(fanboxRepository.getCreatorPosts(creatorId, cursor).contents)
            updateCallback.invoke(posts.size.toFloat() / max)
        }

        updateCallback.invoke(1f)

        val data = posts
            .distinctBy { post -> post.id }
            .map { post ->
                CreatorPostsDownloadData(
                    post = post,
                    isDownloaded = false,
                )
            }

        _screenState.value = _screenState.updateWhenIdle {
            it.copy(
                posts = data,
                targetPosts = data,
                isPrepared = true,
            )
        }
    }

    suspend fun download(postId: PostId): Boolean {
        runCatching {
            val postDetail = fanboxRepository.getPost(postId)

            for (imageItem in postDetail.body.imageItems) {
                suspendCancellableCoroutine {
                    imageDownloader.downloadImage(imageItem) {
                        it.resume(Unit)
                    }
                }
                delay(500)
            }

            for (fileItem in postDetail.body.fileItems) {
                suspendCancellableCoroutine {
                    imageDownloader.downloadFile(fileItem) {
                        it.resume(Unit)
                    }
                }
                delay(500)
            }
        }

        return true
    }

    fun updateIgnoreKeyword(ignoreKeyword: String) {
        val keywords = ignoreKeyword.split(",").map { it.trim() }.filter { it.isNotBlank() }

        _screenState.value = _screenState.updateWhenIdle {
            it.copy(
                targetPosts = it.posts.filter { post ->
                    keywords.none { keyword -> (post.post.title + post.post.excerpt).contains(keyword, ignoreCase = true) }
                },
                ignoreKeyword = ignoreKeyword
            )
        }
    }

    fun updateIgnoreFreePosts(isIgnoreFreePosts: Boolean) {
        _screenState.value = _screenState.updateWhenIdle {
            it.copy(
                targetPosts = it.posts.filter { post -> post.post.feeRequired != 0 },
                isIgnoreFreePosts = isIgnoreFreePosts
            )
        }
    }

    fun updateIgnoreFiles(isIgnoreFiles: Boolean) {
        _screenState.value = _screenState.updateWhenIdle {
            it.copy(
                targetPosts = it.posts.filter { post -> post.post.cover != null },
                isIgnoreFiles = isIgnoreFiles,
            )
        }
    }
}

@Stable
data class CreatorPostsDownloadUiState(
    val creatorDetail: FanboxCreatorDetail,
    val postsPaginate: List<FanboxCursor>,
    val posts: List<CreatorPostsDownloadData>,
    val targetPosts: List<CreatorPostsDownloadData>,
    val ignoreKeyword: String,
    val isIgnoreFreePosts: Boolean,
    val isIgnoreFiles: Boolean,
    val isPrepared: Boolean,
)

@Stable
data class CreatorPostsDownloadData(
    val post: FanboxPost,
    var isDownloaded: Boolean,
)
