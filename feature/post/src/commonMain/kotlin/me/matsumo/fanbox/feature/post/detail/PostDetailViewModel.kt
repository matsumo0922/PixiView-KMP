package me.matsumo.fanbox.feature.post.detail

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.PageOffsetInfo
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxComments
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.FanboxCommentId
import me.matsumo.fanbox.core.model.fanbox.id.FanboxPostId
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.resources.post_detail_comment_comment_failed
import me.matsumo.fanbox.core.resources.post_detail_comment_commented
import me.matsumo.fanbox.core.resources.post_detail_comment_delete_failed
import me.matsumo.fanbox.core.resources.post_detail_comment_delete_success
import org.jetbrains.compose.resources.StringResource

class PostDetailViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val downloadPostsRepository: DownloadPostsRepository,
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

    fun fetch(postId: FanboxPostId) {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                val postDetail = fanboxRepository.getPostDetail(postId)
                val creatorDetail = fanboxRepository.getCreatorDetailCached(postDetail.user.creatorId)
                val comments = fanboxRepository.getPostComment(postId)

                PostDetailUiState(
                    userData = userDataRepository.userData.first(),
                    metaData = fanboxRepository.metaData.first(),
                    postDetail = postDetail,
                    creatorDetail = creatorDetail,
                    comments = comments,
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { ScreenState.Error(Res.string.error_network) },
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

    fun loadMoreComment(postId: FanboxPostId, offset: Int) {
        viewModelScope.launch {
            val comments = fanboxRepository.getPostComment(postId, offset)

            _screenState.value = screenState.updateWhenIdle {
                it.copy(
                    comments = PageOffsetInfo(
                        contents = it.comments.contents + comments.contents,
                        offset = comments.offset,
                    ),
                )
            }
        }
    }

    fun commentLike(commentId: FanboxCommentId) {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.likeComment(commentId)
            }
        }
    }

    fun commentReply(postId: FanboxPostId, body: String, parentFanboxCommentId: FanboxCommentId, rootFanboxCommentId: FanboxCommentId) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                _screenState.value = suspendRunCatching {
                    fanboxRepository.addComment(postId, body, rootFanboxCommentId, parentFanboxCommentId)
                    fanboxRepository.getPostComment(postId)
                }.fold(
                    onSuccess = {
                        ScreenState.Idle(
                            data.data.copy(
                                comments = it,
                                messageToast = Res.string.post_detail_comment_commented,
                            ),
                        )
                    },
                    onFailure = {
                        ScreenState.Idle(
                            data.data.copy(
                                messageToast = Res.string.post_detail_comment_comment_failed,
                            ),
                        )
                    },
                )
            }
        }
    }

    fun commentDelete(postId: FanboxPostId, commentId: FanboxCommentId) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                _screenState.value = suspendRunCatching {
                    fanboxRepository.deleteComment(commentId)
                    fanboxRepository.getPostComment(postId)
                }.fold(
                    onSuccess = {
                        ScreenState.Idle(
                            data.data.copy(
                                comments = it,
                                messageToast = Res.string.post_detail_comment_delete_success,
                            ),
                        )
                    },
                    onFailure = {
                        ScreenState.Idle(
                            data.data.copy(
                                messageToast = Res.string.post_detail_comment_delete_failed,
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

    fun downloadImages(title: String, imageItems: List<FanboxPostDetail.ImageItem>, callback: () -> Unit) {
        downloadPostsRepository.requestDownloadImages(title, imageItems, callback)
    }

    fun downloadFiles(title: String, fileItems: List<FanboxPostDetail.FileItem>, callback: () -> Unit) {
        downloadPostsRepository.requestDownloadFiles(title, fileItems, callback)
    }
}

@Stable
data class PostDetailUiState(
    val userData: UserData,
    val metaData: FanboxMetaData,
    val creatorDetail: FanboxCreatorDetail,
    val postDetail: FanboxPostDetail,
    val comments: PageOffsetInfo<FanboxComments.Item>,
    val messageToast: StringResource? = null,
)
