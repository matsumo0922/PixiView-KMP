package me.matsumo.fanbox.feature.post.detail

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.PageOffsetInfo
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxComments
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.CommentId
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.ads.NativeAdView
import me.matsumo.fanbox.core.ui.common_downloaded
import me.matsumo.fanbox.core.ui.component.pager.HorizontalPager
import me.matsumo.fanbox.core.ui.component.pager.rememberPagerState
import me.matsumo.fanbox.core.ui.error_network
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.extensition.isNullOrEmpty
import me.matsumo.fanbox.core.ui.extensition.marquee
import me.matsumo.fanbox.core.ui.extensition.padding
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import me.matsumo.fanbox.core.ui.view.ErrorView
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.post.detail.items.PostDetailCommentLikeButton
import me.matsumo.fanbox.feature.post.detail.items.PostDetailCreatorSection
import me.matsumo.fanbox.feature.post.detail.items.PostDetailMenuDialog
import me.matsumo.fanbox.feature.post.detail.items.PostDetailTopAppBar
import me.matsumo.fanbox.feature.post.detail.items.postDetailCardSection
import me.matsumo.fanbox.feature.post.detail.items.postDetailCommentItems
import me.matsumo.fanbox.feature.post.detail.items.postDetailItems
import me.matsumo.fanbox.feature.post.detail.items.postDetailTagsSection
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalFoundationApi::class, ExperimentalUuidApi::class)
@Composable
internal fun PostDetailRoute(
    postId: PostId,
    type: PostDetailPagingType,
    navigateToPostSearch: (String, CreatorId) -> Unit,
    navigateToPostDetail: (PostId) -> Unit,
    navigateToPostImage: (PostId, Int) -> Unit,
    navigateToCreatorPlans: (CreatorId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailRootViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paging = uiState.paging?.collectAsLazyPagingItems()

    LaunchedEffect(true) {
        if (paging == null) {
            viewModel.fetch(type)
        }
    }

    if (paging != null && !paging.isNullOrEmpty() && uiState.userData.isUseInfinityPostDetail) {
        LazyPagingItemsLoadContents(
            modifier = modifier,
            lazyPagingItems = paging,
            isSwipeEnabled = false,
        ) {
            val initIndex = remember { paging.itemSnapshotList.indexOfFirst { it?.id == postId } }
            val pagerState = rememberPagerState(if (initIndex != -1) initIndex else 0)

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
                count = paging.itemCount,
                key = { paging[it]?.id?.uniqueValue ?: Uuid.random().toString() },
            ) {
                paging[it]?.let { post ->
                    PostDetailView(
                        modifier = Modifier.fillMaxSize(),
                        postId = post.id,
                        navigateToPostSearch = navigateToPostSearch,
                        navigateToPostDetail = navigateToPostDetail,
                        navigateToPostImage = navigateToPostImage,
                        navigateToCreatorPlans = navigateToCreatorPlans,
                        navigateToCreatorPosts = navigateToCreatorPosts,
                        navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
                        terminate = terminate,
                    )
                }
            }
        }
    } else if (paging != null) {
        PostDetailView(
            modifier = Modifier.fillMaxSize(),
            postId = postId,
            navigateToPostSearch = navigateToPostSearch,
            navigateToPostDetail = navigateToPostDetail,
            navigateToPostImage = navigateToPostImage,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
            terminate = terminate,
        )
    } else {
        ErrorView(
            modifier = Modifier.fillMaxSize(),
            errorState = ScreenState.Error(Res.string.error_network),
            retryAction = { terminate.invoke() },
        )
    }
}

@Composable
private fun PostDetailView(
    postId: PostId,
    navigateToPostSearch: (String, CreatorId) -> Unit,
    navigateToPostDetail: (PostId) -> Unit,
    navigateToPostImage: (PostId, Int) -> Unit,
    navigateToCreatorPlans: (CreatorId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = koinViewModel(key = postId.value),
    navigatorExtension: NavigatorExtension = koinInject(),
    snackExtension: ToastExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(postId) {
        if (screenState !is ScreenState.Idle) {
            viewModel.fetch(postId)
        }
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { viewModel.fetch(postId) },
    ) { uiState ->
        PostDetailScreen(
            modifier = Modifier.fillMaxSize(),
            postDetail = uiState.postDetail,
            comments = uiState.comments,
            creatorDetail = uiState.creatorDetail,
            userData = uiState.userData,
            metaData = uiState.metaData,
            onClickPost = navigateToPostDetail,
            onClickPostLike = {
                PostsLog.like(postId.value).send()
                viewModel.postLike(it)
            },
            onClickPostBookmark = viewModel::postBookmark,
            onClickCommentLoadMore = viewModel::loadMoreComment,
            onClickCommentLike = {
                PostsLog.likeComment(
                    postId = postId.value,
                    commentId = it.value,
                ).send()

                viewModel.commentLike(it)
            },
            onClickCommentReply = { body, parent, root ->
                PostsLog.comment(
                    postId = postId.value,
                    comment = body,
                    parentCommentId = parent.value,
                    rootCommentId = root.value,
                ).send()

                viewModel.commentReply(postId, body, parent, root)
            },
            onClickCommentDelete = {
                navigateToCommentDeleteDialog.invoke(SimpleAlertContents.CommentDelete) {
                    PostsLog.deleteComment(
                        postId = postId.value,
                        commentId = it.value,
                    ).send()

                    viewModel.commentDelete(postId, it)
                }
            },
            onClickTag = { navigateToPostSearch.invoke(it, uiState.postDetail.user.creatorId) },
            onClickCreator = navigateToCreatorPlans,
            onClickImage = { item ->
                uiState.postDetail.body.imageItems.indexOf(item).let { index ->
                    navigateToPostImage.invoke(postId, index)
                }
            },
            onClickFile = {
                viewModel.downloadFiles(listOf(it)) {
                    scope.launch {
                        snackExtension.show(snackbarHostState, Res.string.common_downloaded)
                    }
                }
            },
            onClickDownloadImages = {
                viewModel.downloadImages(it) {
                    scope.launch {
                        snackExtension.show(snackbarHostState, Res.string.common_downloaded)
                    }
                }
            },
            onClickCreatorPosts = navigateToCreatorPosts,
            onClickCreatorPlans = navigateToCreatorPlans,
            onClickFollow = viewModel::follow,
            onClickUnfollow = viewModel::unfollow,
            onClickOpenBrowser = { navigatorExtension.navigateToWebPage(it, PostDetailRoute) },
            onTerminate = terminate,
        )

        LaunchedEffect(uiState.messageToast) {
            uiState.messageToast?.let { scope.launch { snackExtension.show(snackbarHostState, it) } }
            viewModel.consumeToast()
        }
    }
}

@Composable
private fun PostDetailScreen(
    postDetail: FanboxPostDetail,
    comments: PageOffsetInfo<FanboxComments.Item>,
    creatorDetail: FanboxCreatorDetail,
    userData: UserData,
    metaData: FanboxMetaData,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCommentLoadMore: (PostId, Int) -> Unit,
    onClickCommentLike: (CommentId) -> Unit,
    onClickCommentReply: (String, CommentId, CommentId) -> Unit,
    onClickCommentDelete: (CommentId) -> Unit,
    onClickTag: (String) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickFile: (FanboxPostDetail.FileItem) -> Unit,
    onClickDownloadImages: (List<FanboxPostDetail.ImageItem>) -> Unit,
    onClickCreatorPosts: (CreatorId) -> Unit,
    onClickCreatorPlans: (CreatorId) -> Unit,
    onClickFollow: (String) -> Unit,
    onClickUnfollow: (String) -> Unit,
    onClickOpenBrowser: (String) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()

    var isShowMenu by remember { mutableStateOf(false) }
    var isPostLiked by rememberSaveable(postDetail.isLiked) { mutableStateOf(postDetail.isLiked) }
    var isBookmarked by remember(postDetail.isBookmarked) { mutableStateOf(postDetail.isBookmarked) }

    var isShowCommentEditor by remember { mutableStateOf(false) }
    var latestComment by remember { mutableStateOf("") }

    LaunchedEffect(comments) {
        if (isShowCommentEditor) {
            val commentItems = comments.contents.flatMap { comment -> listOf(comment) + comment.replies }
            isShowCommentEditor = !commentItems.any { comment -> comment.user.name == metaData.context.user.name && comment.body == latestComment }
        }
    }

    val isShowHeader = runCatching {
        when (val content = postDetail.body) {
            is FanboxPostDetail.Body.Article -> content.blocks.first() !is FanboxPostDetail.Body.Article.Block.Image
            is FanboxPostDetail.Body.File -> true
            is FanboxPostDetail.Body.Image -> false
            is FanboxPostDetail.Body.Unknown -> true
        }
    }.getOrDefault(true)

    Box(modifier) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            state = state,
        ) {
            if (isShowHeader) {
                item {
                    PostDetailHeader(
                        modifier = Modifier
                            .padding(bottom = 16.dp)
                            .fillMaxWidth(),
                        post = postDetail,
                    )
                }
            }

            postDetailItems(
                post = postDetail,
                userData = userData,
                onClickCreator = onClickCreator,
                onClickPost = onClickPost,
                onClickPostLike = onClickPostLike,
                onClickPostBookmark = onClickPostBookmark,
                onClickImage = onClickImage,
                onClickFile = onClickFile,
                onClickDownload = onClickDownloadImages,
            )

            postDetailTagsSection(
                tags = postDetail.tags.toImmutableList(),
                onClickTag = onClickTag,
            )

            item {
                PostDetailCommentLikeButton(
                    modifier = Modifier
                        .padding(horizontal = 16.dp, top = 16.dp)
                        .fillMaxWidth(),
                    isLiked = isPostLiked,
                    isBookmarked = isBookmarked,
                    likeCount = postDetail.likeCount,
                    commentCount = postDetail.commentCount,
                    onClickLike = {
                        isPostLiked = true
                        onClickPostLike.invoke(postDetail.id)
                    },
                    onClickBookmark = {
                        isBookmarked = it
                        onClickPostBookmark.invoke(postDetail.asPost(), isBookmarked)
                    },
                )
            }

            postDetailCardSection(
                postDetail = postDetail,
                onClickCreatorPlans = onClickCreatorPlans,
                onClickDownloadImages = onClickDownloadImages,
            )

            // Android は NativeAds なので下部に置く
            if (!userData.hasPrivilege && currentPlatform == Platform.Android) {
                item {
                    NativeAdView(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, top = 16.dp)
                            .fillMaxWidth(),
                        key = creatorDetail.creatorId.value,
                    )
                }
            }

            item {
                PostDetailCreatorSection(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    postDetail = postDetail,
                    creatorDetail = creatorDetail,
                    onClickCreator = { onClickCreatorPosts.invoke(it) },
                    onClickFollow = onClickFollow,
                    onClickUnfollow = onClickUnfollow,
                    onClickSupporting = onClickOpenBrowser,
                )
            }

            postDetailCommentItems(
                postDetail = postDetail,
                comments = comments,
                metaData = metaData,
                isShowCommentEditor = isShowCommentEditor,
                onClickLoadMore = onClickCommentLoadMore,
                onClickCommentLike = onClickCommentLike,
                onClickCommentReply = { body, parent, root ->
                    latestComment = body
                    onClickCommentReply.invoke(body, parent, root)
                },
                onClickCommentDelete = onClickCommentDelete,
                onClickShowCommentEditor = { isShowCommentEditor = it },
            )

            item {
                Spacer(modifier = Modifier.height(128.dp))
            }
        }

        PostDetailTopAppBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            state = state,
            postDetail = postDetail,
            isShowHeader = isShowHeader,
            onClickNavigateUp = onTerminate,
            onClickMenu = { isShowMenu = true },
        )
    }

    PostDetailMenuDialog(
        isVisible = isShowMenu,
        onClickOpenBrowser = { onClickOpenBrowser.invoke(postDetail.browserUrl) },
        onClickAllDownload = { onClickDownloadImages.invoke(postDetail.body.imageItems) },
        onDismissRequest = { isShowMenu = false },
    )
}

@Composable
private fun PostDetailHeader(
    post: FanboxPostDetail,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        if (post.coverImageUrl != null) {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4 / 3f),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .fanboxHeader()
                    .data(post.coverImageUrl)
                    .build(),
                loading = {
                    FadePlaceHolder()
                },
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        } else {
            FileThumbnail(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(4 / 3f),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(4 / 3f)
                .align(Alignment.TopCenter)
                .background(Brush.verticalGradient(listOf(Color.Transparent, MaterialTheme.colorScheme.surface))),
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 24.dp, vertical = 16.dp),
        ) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .marquee(),
                text = post.title,
                style = MaterialTheme.typography.headlineSmall.center().bold(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                text = post.publishedDatetime.format("yyyy/MM/dd HH:mm"),
                style = MaterialTheme.typography.bodyMedium.center(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun FileThumbnail(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.AutoMirrored.Filled.InsertDriveFile,
            tint = Color.White,
            contentDescription = null,
        )
    }
}
