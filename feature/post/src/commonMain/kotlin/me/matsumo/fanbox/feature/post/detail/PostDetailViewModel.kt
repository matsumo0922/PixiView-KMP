package me.matsumo.fanbox.feature.post.detail

import androidx.compose.runtime.Stable
import androidx.compose.ui.text.intl.Locale
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.recordException
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.TranslationState
import me.matsumo.fanbox.core.model.fanbox.Comment
import me.matsumo.fanbox.core.model.fanbox.CommentId
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail
import me.matsumo.fanbox.core.model.fanbox.MetaData
import me.matsumo.fanbox.core.model.fanbox.PageOffsetInfo
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostDetail
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.UserId
import me.matsumo.fanbox.core.model.toScreenStateError
import me.matsumo.fanbox.core.model.updateWhenIdle
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.repository.TranslationRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.resources.post_detail_comment_comment_failed
import me.matsumo.fanbox.core.resources.post_detail_comment_commented
import me.matsumo.fanbox.core.resources.post_detail_comment_delete_failed
import me.matsumo.fanbox.core.resources.post_detail_comment_delete_success
import org.jetbrains.compose.resources.StringResource

class PostDetailViewModel(
    private val postId: PostId,
    private val settingRepository: SettingRepository,
    private val fanboxRepository: FanboxRepository,
    private val translationRepository: TranslationRepository,
    private val downloadPostsRepository: DownloadPostsRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<PostDetailUiState>>(ScreenState.Loading)
    val screenState = _screenState.asStateFlow()

    init {
        fetch()

        viewModelScope.launch {
            settingRepository.setting.collectLatest { data ->
                _screenState.updateWhenIdle { it.copy(setting = data) }
            }
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPostsIds.collectLatest { bookmarkedPostsIds ->
                _screenState.updateWhenIdle {
                    it.copy(bookmarkedPostIds = bookmarkedPostsIds)
                }
            }
        }
    }

    fun fetch() {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                val postDetail = fanboxRepository.getPostDetail(postId)
                val creatorDetail = fanboxRepository.getCreatorDetailCached(postDetail.user!!.creatorId!!)
                val comments = runCatching { fanboxRepository.getPostComment(postId) }
                    .onFailure { recordException(it) }
                    .getOrElse { PageOffsetInfo(emptyList(), null) }

                PostDetailUiState(
                    setting = settingRepository.setting.first(),
                    metaData = fanboxRepository.getMetadata(),
                    bookmarkedPostIds = fanboxRepository.bookmarkedPostsIds.first(),
                    postDetail = postDetail,
                    creatorDetail = creatorDetail,
                    comments = comments,
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { it.toScreenStateError() },
            )
        }
    }

    fun translate(postDetail: PostDetail) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                if (data.data.bodyTransState is TranslationState.Translated) return@launch

                _screenState.updateWhenIdle { it.copy(bodyTransState = TranslationState.Loading) }

                val bodyTransState = suspendRunCatching {
                    translationRepository.translate(postDetail, Locale(data.data.setting.translateLanguage))
                }.onSuccess {
                    _screenState.value = ScreenState.Loading
                    delay(500)
                }.fold(
                    onSuccess = { TranslationState.Translated(it) },
                    onFailure = { TranslationState.None },
                )

                _screenState.value = ScreenState.Idle(
                    data.data.copy(
                        postDetail = (bodyTransState as? TranslationState.Translated)?.data ?: data.data.postDetail,
                        bodyTransState = bodyTransState,
                        messageToast = Res.string.error_network.takeIf { bodyTransState is TranslationState.None },
                    ),
                )
            }
        }
    }

    fun translate(comments: PageOffsetInfo<Comment>) {
        viewModelScope.launch {
            (screenState.value as? ScreenState.Idle)?.also { data ->
                _screenState.updateWhenIdle { it.copy(commentsTransState = TranslationState.Loading) }

                val commentsTransState = suspendRunCatching {
                    translationRepository.translate(comments, Locale(data.data.setting.translateLanguage))
                }.fold(
                    onSuccess = { TranslationState.Translated(it) },
                    onFailure = { TranslationState.None },
                )

                _screenState.value = ScreenState.Loading
                _screenState.value = ScreenState.Idle(
                    data.data.copy(
                        comments = (commentsTransState as? TranslationState.Translated)?.data ?: data.data.comments,
                        commentsTransState = commentsTransState,
                        messageToast = Res.string.error_network.takeIf { commentsTransState is TranslationState.None },
                    ),
                )
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

    fun loadMoreComment(postId: PostId, offset: Int) {
        viewModelScope.launch {
            suspendRunCatching {
                val comments = fanboxRepository.getPostComment(postId, offset)

                _screenState.updateWhenIdle {
                    it.copy(
                        comments = PageOffsetInfo(
                            contents = it.comments.contents + comments.contents,
                            offset = comments.offset,
                        ),
                    )
                }
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

    fun commentReply(postId: PostId, body: String, parentFanboxCommentId: CommentId, rootFanboxCommentId: CommentId) {
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

    fun commentDelete(postId: PostId, commentId: CommentId) {
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

    fun follow(creatorUserId: UserId) {
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

    fun unfollow(creatorUserId: UserId) {
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

    fun downloadImages(postId: PostId, title: String, imageItems: List<PostDetail.ImageItem>) {
        downloadPostsRepository.requestDownloadImages(postId, title, imageItems)
    }

    fun downloadFiles(postId: PostId, title: String, fileItems: List<PostDetail.FileItem>) {
        downloadPostsRepository.requestDownloadFiles(postId, title, fileItems)
    }
}

@Stable
data class PostDetailUiState(
    val setting: Setting,
    val metaData: MetaData,
    val bookmarkedPostIds: List<PostId>,
    val creatorDetail: CreatorDetail,
    val postDetail: PostDetail,
    val comments: PageOffsetInfo<Comment>,
    val bodyTransState: TranslationState<PostDetail> = TranslationState.None,
    val commentsTransState: TranslationState<PageOffsetInfo<Comment>> = TranslationState.None,
    val messageToast: StringResource? = null,
)
