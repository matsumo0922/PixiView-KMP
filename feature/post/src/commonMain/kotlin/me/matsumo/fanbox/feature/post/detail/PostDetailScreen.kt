package me.matsumo.fanbox.feature.post.detail

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
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.resources.queue_added
import me.matsumo.fanbox.core.resources.queue_added_action
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.ads.BannerAdView
import me.matsumo.fanbox.core.ui.ads.NativeAdView
import me.matsumo.fanbox.core.ui.component.pager.HorizontalPager
import me.matsumo.fanbox.core.ui.component.pager.rememberPagerState
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
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
import me.matsumo.fanbox.feature.post.detail.items.PostDetailBottomBar
import me.matsumo.fanbox.feature.post.detail.items.PostDetailCommentLikeButton
import me.matsumo.fanbox.feature.post.detail.items.PostDetailCreatorSection
import me.matsumo.fanbox.feature.post.detail.items.PostDetailMenuDialog
import me.matsumo.fanbox.feature.post.detail.items.PostDetailTopAppBar
import me.matsumo.fanbox.feature.post.detail.items.postDetailCardSection
import me.matsumo.fanbox.feature.post.detail.items.postDetailCommentItems
import me.matsumo.fanbox.feature.post.detail.items.postDetailItems
import me.matsumo.fanbox.feature.post.detail.items.postDetailTagsSection
import me.matsumo.fankt.fanbox.domain.PageOffsetInfo
import me.matsumo.fankt.fanbox.domain.model.FanboxComment
import me.matsumo.fankt.fanbox.domain.model.FanboxCover
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCommentId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun PostDetailRoute(
    postId: FanboxPostId,
    type: PostDetailPagingType,
    navigateToPostSearch: (String, FanboxCreatorId) -> Unit,
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToPostImage: (FanboxPostId, Int) -> Unit,
    navigateToCreatorPlans: (FanboxCreatorId) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    navigateToDownloadQueue: () -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailRootViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paging = uiState.paging?.collectAsLazyPagingItems()

    val postDetailMap = remember { mutableStateMapOf<FanboxPostId, FanboxPostDetail>() }
    var currentPostId by remember { mutableStateOf(postId) }

    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(true) {
        if (paging == null) {
            viewModel.fetch(type)
        }
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            Column(
                modifier = Modifier.background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
            ) {
                if (!uiState.userData.hasPrivilege) {
                    BannerAdView(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

                PostDetailBottomBar(
                    modifier = Modifier.fillMaxWidth(),
                    postDetail = postDetailMap[currentPostId],
                    isBookmarked = uiState.bookmarkedPostIds.contains(currentPostId),
                    onCreatorClicked = navigateToCreatorPosts,
                    onBookmarkClicked = { isBookmarked ->
                        postDetailMap[currentPostId]?.let { viewModel.postBookmark(it.adPost(), isBookmarked) }
                    },
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
    ) { padding ->
        if (paging != null && !paging.isNullOrEmpty() && uiState.userData.isUseInfinityPostDetail) {
            LazyPagingItemsLoadContents(
                modifier = Modifier.padding(padding),
                lazyPagingItems = paging,
                isSwipeEnabled = false,
            ) {
                val initIndex = remember { paging.itemSnapshotList.indexOfFirst { it?.id == postId } }
                val pagerState = rememberPagerState(if (initIndex != -1) initIndex else 0)

                LaunchedEffect(pagerState) {
                    snapshotFlow { pagerState.currentPage }.collect { index ->
                        paging[index]?.id?.let { currentPostId = it }
                    }
                }

                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState,
                    count = paging.itemCount,
                    key = { index -> paging[index]?.id?.uniqueValue ?: Uuid.random().toString() },
                ) { index ->
                    paging[index]?.let { post ->
                        PostDetailView(
                            modifier = Modifier.fillMaxSize(),
                            postId = post.id,
                            snackbarHostState = snackbarHostState,
                            navigateToPostSearch = navigateToPostSearch,
                            navigateToPostDetail = navigateToPostDetail,
                            navigateToPostImage = navigateToPostImage,
                            navigateToCreatorPlans = navigateToCreatorPlans,
                            navigateToCreatorPosts = navigateToCreatorPosts,
                            navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
                            navigateToDownloadQueue = navigateToDownloadQueue,
                            onPostDetailFetched = { postDetailMap[post.id] = it },
                            terminate = terminate,
                        )
                    }
                }
            }
        } else if (paging != null) {
            PostDetailView(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                postId = postId,
                snackbarHostState = snackbarHostState,
                navigateToPostSearch = navigateToPostSearch,
                navigateToPostDetail = navigateToPostDetail,
                navigateToPostImage = navigateToPostImage,
                navigateToCreatorPlans = navigateToCreatorPlans,
                navigateToCreatorPosts = navigateToCreatorPosts,
                navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
                navigateToDownloadQueue = navigateToDownloadQueue,
                onPostDetailFetched = { postDetailMap[postId] = it },
                terminate = terminate,
            )
        } else {
            ErrorView(
                modifier = Modifier
                    .padding(padding)
                    .fillMaxSize(),
                errorState = ScreenState.Error(Res.string.error_network),
                retryAction = { terminate.invoke() },
                terminate = { terminate.invoke() },
            )
        }
    }
}

@Composable
private fun PostDetailView(
    postId: FanboxPostId,
    snackbarHostState: SnackbarHostState,
    navigateToPostSearch: (String, FanboxCreatorId) -> Unit,
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToPostImage: (FanboxPostId, Int) -> Unit,
    navigateToCreatorPlans: (FanboxCreatorId) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    navigateToDownloadQueue: () -> Unit,
    onPostDetailFetched: (FanboxPostDetail) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = koinViewModel(key = postId.value),
    navigatorExtension: NavigatorExtension = koinInject(),
    snackExtension: ToastExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
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
        terminate = { terminate.invoke() },
    ) { uiState ->
        PostDetailScreen(
            modifier = Modifier.fillMaxSize(),
            postDetail = uiState.postDetail,
            comments = uiState.comments,
            creatorDetail = uiState.creatorDetail,
            userData = uiState.userData,
            metaData = uiState.metaData,
            bookmarkedPostIds = uiState.bookmarkedPostIds.toImmutableList(),
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
                    parentFanboxCommentId = parent.value,
                    rootFanboxCommentId = root.value,
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
            onClickTag = { tag ->
                uiState.postDetail.user?.creatorId?.let { navigateToPostSearch.invoke(tag, it) }
            },
            onClickCreator = navigateToCreatorPlans,
            onClickImage = { item ->
                uiState.postDetail
                uiState.postDetail.body.imageItems.indexOf(item).let { index ->
                    navigateToPostImage.invoke(postId, index)
                }
            },
            onClickFile = {
                scope.launch {
                    viewModel.downloadFiles(uiState.postDetail.id, uiState.postDetail.title, listOf(it))
                    snackExtension.show(
                        snackbarHostState = snackbarHostState,
                        message = Res.string.queue_added,
                        label = Res.string.queue_added_action,
                        callback = { result ->
                            if (result == SnackbarResult.ActionPerformed) {
                                navigateToDownloadQueue.invoke()
                            }
                        },
                    )
                }
            },
            onClickDownloadImages = {
                scope.launch {
                    viewModel.downloadImages(uiState.postDetail.id, uiState.postDetail.title, it)
                    snackExtension.show(
                        snackbarHostState = snackbarHostState,
                        message = Res.string.queue_added,
                        label = Res.string.queue_added_action,
                        callback = { result ->
                            if (result == SnackbarResult.ActionPerformed) {
                                navigateToDownloadQueue.invoke()
                            }
                        },
                    )
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

        LaunchedEffect(uiState.postDetail) {
            onPostDetailFetched.invoke(uiState.postDetail)
        }
    }
}

@Composable
private fun PostDetailScreen(
    postDetail: FanboxPostDetail,
    comments: PageOffsetInfo<FanboxComment>,
    creatorDetail: FanboxCreatorDetail,
    bookmarkedPostIds: ImmutableList<FanboxPostId>,
    userData: UserData,
    metaData: FanboxMetaData,
    onClickPost: (FanboxPostId) -> Unit,
    onClickPostLike: (FanboxPostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCommentLoadMore: (FanboxPostId, Int) -> Unit,
    onClickCommentLike: (FanboxCommentId) -> Unit,
    onClickCommentReply: (String, FanboxCommentId, FanboxCommentId) -> Unit,
    onClickCommentDelete: (FanboxCommentId) -> Unit,
    onClickTag: (String) -> Unit,
    onClickCreator: (FanboxCreatorId) -> Unit,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickFile: (FanboxPostDetail.FileItem) -> Unit,
    onClickDownloadImages: (List<FanboxPostDetail.ImageItem>) -> Unit,
    onClickCreatorPosts: (FanboxCreatorId) -> Unit,
    onClickCreatorPlans: (FanboxCreatorId) -> Unit,
    onClickFollow: (FanboxUserId) -> Unit,
    onClickUnfollow: (FanboxUserId) -> Unit,
    onClickOpenBrowser: (String) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()

    var isShowMenu by remember { mutableStateOf(false) }
    var isPostLiked by rememberSaveable(postDetail.isLiked) { mutableStateOf(postDetail.isLiked) }
    val isBookmarked by remember(bookmarkedPostIds) { mutableStateOf(bookmarkedPostIds.contains(postDetail.id)) }

    var isShowCommentEditor by remember { mutableStateOf(false) }
    var latestComment by remember { mutableStateOf("") }

    LaunchedEffect(comments) {
        if (isShowCommentEditor) {
            val commentItems = comments.contents.flatMap { comment -> listOf(comment) + comment.replies }
            isShowCommentEditor = !commentItems.any { comment -> comment.user?.name == metaData.context.user.name && comment.body == latestComment }
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
                isBookmarked = isBookmarked,
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
                        onClickPostBookmark.invoke(postDetail.adPost(), !isBookmarked)
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

private fun FanboxPostDetail.adPost(): FanboxPost {
    return FanboxPost(
        id = id,
        title = title,
        excerpt = excerpt,
        cover = FanboxCover(
            url = coverImageUrl ?: body.imageItems.firstOrNull()?.thumbnailUrl.orEmpty(),
            type = "From Detail",
        ),
        hasAdultContent = hasAdultContent,
        isLiked = isLiked,
        isRestricted = isRestricted,
        likeCount = likeCount,
        commentCount = commentCount,
        updatedDatetime = updatedDatetime,
        publishedDatetime = publishedDatetime,
        feeRequired = feeRequired,
        user = user,
        tags = tags,
    )
}
