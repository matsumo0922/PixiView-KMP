package me.matsumo.fanbox.feature.post.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.draw.shadow
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
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.shapes.balloon.Arrow
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.model.TranslationState
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.resources.queue_added
import me.matsumo.fanbox.core.resources.queue_added_action
import me.matsumo.fanbox.core.resources.reveal_post_detail_translate
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.ads.BannerAdView
import me.matsumo.fanbox.core.ui.ads.NativeAdView
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.LocalRevealCanvasState
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.OverlayText
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.extensition.isNullOrEmpty
import me.matsumo.fanbox.core.ui.extensition.marquee
import me.matsumo.fanbox.core.ui.extensition.padding
import me.matsumo.fanbox.core.ui.extensition.revealByStep
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import me.matsumo.fanbox.core.ui.view.ErrorView
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

internal enum class PostDetailRevealKeys {
    Translate,
}

@OptIn(ExperimentalUuidApi::class)
@Composable
internal fun PostDetailRoute(
    postId: FanboxPostId,
    navigateTo: (Destination) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailRootViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val paging = uiState.paging?.collectAsLazyPagingItems()

    val postDetailMap = remember { mutableStateMapOf<FanboxPostId, FanboxPostDetail>() }
    var currentPostId by remember { mutableStateOf(postId) }
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    val revealCanvasState = LocalRevealCanvasState.current
    val revealState = rememberRevealState()
    val revealOverlayContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    val revealOverlayContentColor = MaterialTheme.colorScheme.onTertiaryContainer

    Reveal(
        modifier = modifier,
        onOverlayClick = { scope.launch { revealState.hide() } },
        revealCanvasState = revealCanvasState,
        revealState = revealState,
        overlayContent = { key -> RevealOverlayContent(key, revealOverlayContainerColor, revealOverlayContentColor) },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Column(
                    modifier = Modifier
                        .shadow(8.dp)
                        .background(MaterialTheme.colorScheme.surfaceColorAtElevation(1.dp)),
                ) {
                    if (!uiState.setting.hasPrivilege) {
                        BannerAdView(
                            modifier = Modifier.fillMaxWidth(),
                        )
                    }

                    PostDetailBottomBar(
                        modifier = Modifier.fillMaxWidth(),
                        postDetail = postDetailMap[currentPostId],
                        isBookmarked = uiState.bookmarkedPostIds.contains(currentPostId),
                        onCreatorClicked = { navigateTo(Destination.CreatorTop(it, true)) },
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
            contentWindowInsets = WindowInsets(0, 0, 0, 0),
        ) { padding ->
            if (paging != null && !paging.isNullOrEmpty()) {
                LazyPagingItemsLoadContents(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding),
                    lazyPagingItems = paging,
                    isSwipeEnabled = false,
                ) {
                    val initIndex = remember { paging.itemSnapshotList.indexOfFirst { it == postId } }
                    val pagerState = rememberPagerState(initialPage = initIndex) { paging.itemCount }

                    LaunchedEffect(pagerState) {
                        snapshotFlow { pagerState.currentPage }.collect { index ->
                            paging[index]?.let { currentPostId = it }
                        }
                    }

                    HorizontalPager(
                        modifier = Modifier.fillMaxSize(),
                        state = pagerState,
                        key = { index -> paging[index]?.uniqueValue ?: Uuid.random().toString() },
                        userScrollEnabled = uiState.setting.isUseInfinityPostDetail,
                    ) { index ->
                        paging[index]?.let { id ->
                            PostDetailView(
                                modifier = Modifier.fillMaxSize(),
                                postId = id,
                                shouldShowReveal = uiState.shouldShowReveal,
                                revealState = revealState,
                                snackbarHostState = snackbarHostState,
                                navigateTo = navigateTo,
                                navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
                                onPostDetailFetched = { postDetailMap[id] = it },
                                onRevealCompleted = viewModel::finishReveal,
                                terminate = terminate,
                            )
                        }
                    }
                }
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
}

@Composable
private fun PostDetailView(
    postId: FanboxPostId,
    shouldShowReveal: Boolean,
    revealState: RevealState,
    snackbarHostState: SnackbarHostState,
    navigateTo: (Destination) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    onPostDetailFetched: (FanboxPostDetail) -> Unit,
    onRevealCompleted: () -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = koinViewModel(key = postId.value) {
        parametersOf(postId)
    },
    navigatorExtension: NavigatorExtension = koinInject(),
    snackExtension: ToastExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { viewModel.fetch() },
        terminate = { terminate.invoke() },
    ) { uiState ->
        PostDetailScreen(
            modifier = Modifier.fillMaxSize(),
            revealState = revealState,
            shouldShowReveal = shouldShowReveal,
            postDetail = uiState.postDetail,
            comments = uiState.comments,
            creatorDetail = uiState.creatorDetail,
            setting = uiState.setting,
            metaData = uiState.metaData,
            bookmarkedPostIds = uiState.bookmarkedPostIds.toImmutableList(),
            bodyTransState = uiState.bodyTransState,
            commentsTransState = uiState.commentsTransState,
            onClickBodyTranslate = viewModel::translate,
            onClickCommentsTranslate = viewModel::translate,
            onClickPost = { navigateTo(Destination.PostDetail(it, Destination.PostDetail.PagingType.Unknown)) },
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
                uiState.postDetail.user?.creatorId?.let { creatorId ->
                    navigateTo(Destination.PostSearch(creatorId, null, tag))
                }
            },
            onClickCreator = { navigateTo(Destination.CreatorTop(it, false)) },
            onClickImage = { item ->
                uiState.postDetail.body.imageItems.indexOf(item).let { index ->
                    navigateTo(Destination.PostImage(postId, index))
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
                                navigateTo(Destination.DownloadQueue)
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
                                navigateTo(Destination.DownloadQueue)
                            }
                        },
                    )
                }
            },
            onClickCreatorPosts = { navigateTo(Destination.CreatorTop(it, true)) },
            onClickCreatorPlans = { navigateTo(Destination.CreatorTop(it, false)) },
            onClickFollow = viewModel::follow,
            onClickUnfollow = viewModel::unfollow,
            onClickOpenBrowser = { navigatorExtension.navigateToWebPage(it, "") },
            onClickBillingPlus = { navigateTo(Destination.BillingPlusBottomSheet(it)) },
            onRevealCompleted = onRevealCompleted,
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
    revealState: RevealState,
    postDetail: FanboxPostDetail,
    comments: PageOffsetInfo<FanboxComment>,
    creatorDetail: FanboxCreatorDetail,
    bookmarkedPostIds: ImmutableList<FanboxPostId>,
    setting: Setting,
    metaData: FanboxMetaData,
    bodyTransState: TranslationState<FanboxPostDetail>,
    commentsTransState: TranslationState<PageOffsetInfo<FanboxComment>>,
    shouldShowReveal: Boolean,
    onClickBodyTranslate: (FanboxPostDetail) -> Unit,
    onClickCommentsTranslate: (PageOffsetInfo<FanboxComment>) -> Unit,
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
    onClickBillingPlus: (String?) -> Unit,
    onRevealCompleted: () -> Unit,
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
            isShowCommentEditor = !commentItems.any { comment -> comment.user?.name == metaData.context?.user?.name && comment.body == latestComment }
        }
    }

    if (shouldShowReveal) {
        LaunchedEffect(shouldShowReveal) {
            delay(500)
            revealState.revealByStep(
                keys = listOf(PostDetailRevealKeys.Translate),
                onCompleted = onRevealCompleted,
            )
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
                setting = setting,
                bookmarkedPostIds = bookmarkedPostIds,
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
            if (!setting.hasPrivilege && currentPlatform == Platform.Android) {
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
                commentsTransState = commentsTransState,
                isShowCommentEditor = isShowCommentEditor,
                onClickLoadMore = onClickCommentLoadMore,
                onClickCommentLike = onClickCommentLike,
                onClickCommentReply = { body, parent, root ->
                    latestComment = body
                    onClickCommentReply.invoke(body, parent, root)
                },
                onClickCommentDelete = onClickCommentDelete,
                onClickShowCommentEditor = { isShowCommentEditor = it },
                onClickTranslate = {
                    if (setting.hasPrivilege) {
                        onClickCommentsTranslate.invoke(it)
                    } else {
                        onClickBillingPlus.invoke("translate")
                    }
                },
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
            revealState = revealState,
            postDetail = postDetail,
            bodyTransState = bodyTransState,
            isShowHeader = isShowHeader,
            onClickNavigateUp = onTerminate,
            onClickTranslate = {
                if (setting.hasPrivilege) {
                    onClickBodyTranslate.invoke(postDetail)
                } else {
                    onClickBillingPlus.invoke("translate")
                }
            },
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

@Composable
private fun RevealOverlayScope.RevealOverlayContent(
    key: Key,
    containerColor: Color,
    contentColor: Color,
) {
    when (key) {
        PostDetailRevealKeys.Translate -> {
            OverlayText(
                modifier = Modifier.align(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = RevealOverlayArrangement.Bottom,
                ),
                text = stringResource(Res.string.reveal_post_detail_translate, appName),
                arrow = Arrow.top(horizontalAlignment = Alignment.End),
                containerColor = containerColor,
                contentColor = contentColor,
            )
        }
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
