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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
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
import app.cash.paging.compose.collectAsLazyPagingItems
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.CommentId
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.ads.BannerAdView
import me.matsumo.fanbox.core.ui.ads.NativeAdView
import me.matsumo.fanbox.core.ui.component.CoordinatorScaffold
import me.matsumo.fanbox.core.ui.component.RestrictCardItem
import me.matsumo.fanbox.core.ui.component.TagItems
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtension
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.extensition.isNullOrEmpty
import me.matsumo.fanbox.core.ui.extensition.marquee
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import me.matsumo.fanbox.core.ui.view.ErrorView
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.post.detail.items.PostDetailArticleHeader
import me.matsumo.fanbox.feature.post.detail.items.PostDetailCommentLikeButton
import me.matsumo.fanbox.feature.post.detail.items.PostDetailCreatorSection
import me.matsumo.fanbox.feature.post.detail.items.PostDetailDownloadSection
import me.matsumo.fanbox.feature.post.detail.items.PostDetailFileHeader
import me.matsumo.fanbox.feature.post.detail.items.PostDetailImageHeader
import me.matsumo.fanbox.feature.post.detail.items.PostDetailMenuDialog
import me.matsumo.fanbox.feature.post.detail.items.postDetailCommentItems
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.compose.koinInject

@OptIn(ExperimentalFoundationApi::class)
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
    viewModel: PostDetailRootViewModel = koinViewModel(PostDetailRootViewModel::class),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paging = uiState.paging?.collectAsLazyPagingItems()

    LaunchedEffect(true) {
        if (paging == null) {
            viewModel.fetch(type)
        }
    }

    if (!paging.isNullOrEmpty() && uiState.userData.isUseInfinityPostDetail) {
        LazyPagingItemsLoadContents(
            modifier = modifier,
            lazyPagingItems = paging!!,
        ) {
            val initIndex = remember { paging.itemSnapshotList.indexOfFirst { it?.id == postId } }
            val pagerState = rememberPagerState(if (initIndex != -1) initIndex else 0) { paging.itemCount }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                state = pagerState,
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
            modifier = modifier,
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
            errorState = ScreenState.Error(MR.strings.error_network),
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
    viewModel: PostDetailViewModel = koinViewModel(PostDetailViewModel::class, key = postId.value),
    navigatorExtension: NavigatorExtension = koinInject(),
    snackExtension: SnackbarExtension = koinInject(),
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
            creatorDetail = uiState.creatorDetail,
            userData = uiState.userData,
            metaData = uiState.metaData,
            onClickPost = navigateToPostDetail,
            onClickPostLike = viewModel::postLike,
            onClickPostBookmark = viewModel::postBookmark,
            onClickCommentLoadMore = viewModel::loadMoreComment,
            onClickCommentLike = viewModel::commentLike,
            onClickCommentReply = viewModel::commentReply,
            onClickCommentDelete = {
                navigateToCommentDeleteDialog.invoke(SimpleAlertContents.CommentDelete) {
                    viewModel.commentDelete(it)
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
                scope.launch {
                    val result = if (viewModel.downloadFiles(listOf(it))) MR.strings.common_downloaded else MR.strings.error_download
                    snackExtension.showSnackbar(snackbarHostState, result)
                }
            },
            onClickDownloadImages = {
                scope.launch {
                    val result = if (viewModel.downloadImages(it)) MR.strings.common_downloaded else MR.strings.error_download
                    snackExtension.showSnackbar(snackbarHostState, result)
                }
            },
            onClickCreatorPosts = navigateToCreatorPosts,
            onClickCreatorPlans = navigateToCreatorPlans,
            onClickFollow = viewModel::follow,
            onClickUnfollow = viewModel::unfollow,
            onClickOpenBrowser = navigatorExtension::navigateToWebPage,
            onTerminate = terminate,
        )

        LaunchedEffect(uiState.messageToast) {
            uiState.messageToast?.let { scope.launch { snackExtension.showSnackbar(snackbarHostState, it) } }
            viewModel.consumeToast()
        }
    }
}

@Composable
private fun PostDetailScreen(
    postDetail: FanboxPostDetail,
    creatorDetail: FanboxCreatorDetail,
    userData: UserData,
    metaData: FanboxMetaData,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCommentLoadMore: (PostId, Int) -> Unit,
    onClickCommentLike: (CommentId) -> Unit,
    onClickCommentReply: (PostId, String, CommentId, CommentId) -> Unit,
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
    var isShowMenu by remember { mutableStateOf(false) }
    var isPostLiked by rememberSaveable(postDetail.isLiked) { mutableStateOf(postDetail.isLiked) }
    var isBookmarked by remember(postDetail.isBookmarked) { mutableStateOf(postDetail.isBookmarked) }

    var isShowCommentEditor by remember { mutableStateOf(false) }
    var latestComment by remember { mutableStateOf("") }

    LaunchedEffect(postDetail.commentList) {
        if (isShowCommentEditor) {
            val comments = postDetail.commentList.contents.flatMap { comment -> listOf(comment) + comment.replies }
            isShowCommentEditor = !comments.any { comment -> comment.user.name == metaData.context.user.name && comment.body == latestComment }
        }
    }

    val isShowCoordinateHeader = when (val content = postDetail.body) {
        is FanboxPostDetail.Body.Article -> content.blocks.first() !is FanboxPostDetail.Body.Article.Block.Image
        is FanboxPostDetail.Body.File -> true
        is FanboxPostDetail.Body.Image -> false
        is FanboxPostDetail.Body.Unknown -> true
    }

    CoordinatorScaffold(
        modifier = modifier,
        onClickNavigateUp = onTerminate,
        onClickMenu = { isShowMenu = true },
        verticalArrangement = Arrangement.spacedBy(16.dp),
        header = {
            if (!isShowCoordinateHeader) {
                PostDetailContent(
                    modifier = it,
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
            } else {
                PostDetailHeader(
                    modifier = it,
                    post = postDetail,
                )
            }
        },
        bottomBar = {
            if (!userData.hasPrivilege) {
                BannerAdView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding(),
                )
            }
        }
    ) {
        if (isShowCoordinateHeader) {
            item {
                PostDetailContent(
                    modifier = Modifier.fillMaxWidth(),
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
            }
        }

        if (postDetail.tags.isNotEmpty()) {
            item {
                TagItems(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    tags = postDetail.tags.toImmutableList(),
                    onClickTag = onClickTag,
                )
            }
        }

        item {
            PostDetailCommentLikeButton(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
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

        if (postDetail.isRestricted) {
            item {
                RestrictCardItem(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    feeRequired = postDetail.feeRequired,
                    backgroundColor = MaterialTheme.colorScheme.surfaceColorAtElevation(8.dp),
                    onClickPlanList = { onClickCreatorPlans.invoke(postDetail.user.creatorId) },
                )
            }
        } else if (postDetail.body.imageItems.isNotEmpty()) {
            item {
                PostDetailDownloadSection(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    postDetail = postDetail,
                    onClickDownload = onClickDownloadImages,
                )
            }
        }

        // Android は NativeAds なので下部に置く
        if (!userData.hasPrivilege && currentPlatform == Platform.Android) {
            item {
                NativeAdView(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    key = creatorDetail.creatorId.value
                )
            }
        }

        item {
            PostDetailCreatorSection(
                modifier = Modifier.fillMaxWidth(),
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
            metaData = metaData,
            isShowCommentEditor = isShowCommentEditor,
            onClickLoadMore = onClickCommentLoadMore,
            onClickCommentLike = onClickCommentLike,
            onClickCommentReply = { body, parent, root ->
                latestComment = body
                onClickCommentReply.invoke(postDetail.id, body, parent, root)
            },
            onClickCommentDelete = onClickCommentDelete,
            onClickShowCommentEditor = { isShowCommentEditor = it },
        )

        item {
            Spacer(modifier = Modifier.height(128.dp))
        }
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
private fun PostDetailContent(
    post: FanboxPostDetail,
    userData: UserData,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickFile: (FanboxPostDetail.FileItem) -> Unit,
    onClickDownload: (List<FanboxPostDetail.ImageItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        when (val content = post.body) {
            is FanboxPostDetail.Body.Article -> {
                PostDetailArticleHeader(
                    modifier = Modifier.fillMaxWidth(),
                    content = content,
                    userData = userData,
                    isAdultContents = post.hasAdultContent,
                    onClickPost = onClickPost,
                    onClickPostLike = onClickPostLike,
                    onClickPostBookmark = onClickPostBookmark,
                    onClickCreator = onClickCreator,
                    onClickImage = onClickImage,
                    onClickFile = onClickFile,
                    onClickDownload = onClickDownload,
                )
            }

            is FanboxPostDetail.Body.Image -> {
                PostDetailImageHeader(
                    modifier = Modifier.fillMaxWidth(),
                    content = content,
                    isAdultContents = post.hasAdultContent,
                    isOverrideAdultContents = userData.isAllowedShowAdultContents,
                    isTestUser = userData.isTestUser,
                    onClickImage = onClickImage,
                    onClickDownload = onClickDownload,
                )
            }

            is FanboxPostDetail.Body.File -> {
                PostDetailFileHeader(
                    modifier = Modifier.fillMaxWidth(),
                    content = content,
                    onClickFile = onClickFile,
                )
            }

            is FanboxPostDetail.Body.Unknown -> {
                // do nothing
            }
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
            imageVector = Icons.Filled.InsertDriveFile,
            tint = Color.White,
            contentDescription = null,
        )
    }
}
