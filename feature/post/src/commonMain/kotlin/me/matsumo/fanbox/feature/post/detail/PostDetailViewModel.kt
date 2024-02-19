package me.matsumo.fanbox.feature.post.detail

import androidx.compose.runtime.Stable
import dev.icerock.moko.resources.StringResource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.PageOffsetInfo
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.CommentId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.extensition.ImageDownloader
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class PostDetailViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val imageDownloader: ImageDownloader,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<PostDetailUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest { data ->
                _screenState.value = screenState.updateWhenIdle { it.copy(userData = data) }
            }
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPosts.collectLatest { bookmarkedPosts ->
                _screenState.value = screenState.updateWhenIdle {
                    it.copy(postDetail = it.postDetail.copy(isBookmarked = it.postDetail.id in bookmarkedPosts))
                }
            }
        }
    }

    fun fetch(postId: PostId) {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                val postDetail = fanboxRepository.getPost(postId)
                val creatorDetail = fanboxRepository.getCreatorCached(postDetail.user.creatorId)

                PostDetailUiState(
                    userData = userDataRepository.userData.first(),
                    metaData = fanboxRepository.metaData.first(),
                    postDetail = postDetail,
                    creatorDetail = creatorDetail,
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { ScreenState.Error(MR.strings.error_network) },
            )
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPosts.collectLatest { bookmarkedPosts ->
                _screenState.value = screenState.updateWhenIdle {
                    it.copy(postDetail = it.postDetail.copy(isBookmarked = it.postDetail.id in bookmarkedPosts))
                }
            }
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

    fun loadMoreComment(postId: PostId, offset: Int) {
        viewModelScope.launch {
            val comments = fanboxRepository.getPostComment(postId, offset)

            _screenState.value = screenState.updateWhenIdle {
                it.copy(
                    postDetail = it.postDetail.copy(
                        commentList = PageOffsetInfo(
                            contents = it.postDetail.commentList.contents + comments.contents,
                            offset = comments.offset,
                        ),
                    ),
                )
            }
        }
    }

    fun commentLike(commentId: CommentId) {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.likeComment(commentId)
            }
        }
    }

    fun commentReply(postId: PostId, body: String, parentCommentId: CommentId, rootCommentId: CommentId) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                _screenState.value = suspendRunCatching {
                    fanboxRepository.addComment(postId, body, rootCommentId, parentCommentId)
                    fanboxRepository.getPost(postId)
                }.fold(
                    onSuccess = {
                        ScreenState.Idle(
                            data.data.copy(
                                postDetail = it,
                                messageToast = MR.strings.post_detail_comment_commented,
                            ),
                        )
                    },
                    onFailure = {
                        ScreenState.Idle(
                            data.data.copy(
                                messageToast = MR.strings.post_detail_comment_comment_failed,
                            ),
                        )
                    },
                )
            }
        }
    }

    fun commentDelete(commentId: CommentId) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                _screenState.value = suspendRunCatching {
                    fanboxRepository.deleteComment(commentId)
                    fanboxRepository.getPost(data.data.postDetail.id)
                }.fold(
                    onSuccess = {
                        ScreenState.Idle(
                            data.data.copy(
                                postDetail = it,
                                messageToast = MR.strings.post_detail_comment_delete_success,
                            ),
                        )
                    },
                    onFailure = {
                        ScreenState.Idle(
                            data.data.copy(
                                messageToast = MR.strings.post_detail_comment_delete_failed,
                            ),
                        )
                    },
                )
            }
        }
    }

    fun follow(creatorUserId: String) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                val creatorDetail = suspendRunCatching {
                    fanboxRepository.followCreator(creatorUserId)
                }.fold(
                    onSuccess = { data.data.creatorDetail.copy(isFollowed = true) },
                    onFailure = { data.data.creatorDetail.copy(isFollowed = false) },
                )

                _screenState.value = ScreenState.Idle(data.data.copy(creatorDetail = creatorDetail))
            }
        }
    }

    fun unfollow(creatorUserId: String) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                val creatorDetail = suspendRunCatching {
                    fanboxRepository.unfollowCreator(creatorUserId)
                }.fold(
                    onSuccess = { data.data.creatorDetail.copy(isFollowed = false) },
                    onFailure = { data.data.creatorDetail.copy(isFollowed = true) },
                )

                _screenState.value = ScreenState.Idle(data.data.copy(creatorDetail = creatorDetail))
            }
        }
    }

    fun consumeToast() {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                _screenState.value = ScreenState.Idle(data.data.copy(messageToast = null))
            }
        }
    }

    suspend fun downloadImages(imageItems: List<FanboxPostDetail.ImageItem>): Boolean {
        return imageItems.map { imageDownloader.downloadImage(it) }.all { it }
    }

    suspend fun downloadFiles(fileItems: List<FanboxPostDetail.FileItem>): Boolean {
        return fileItems.map { imageDownloader.downloadFile(it) }.all { it }
    }
}

@Stable
data class PostDetailUiState(
    val userData: UserData,
    val metaData: FanboxMetaData,
    val creatorDetail: FanboxCreatorDetail,
    val postDetail: FanboxPostDetail,
    val messageToast: StringResource? = null,
)
