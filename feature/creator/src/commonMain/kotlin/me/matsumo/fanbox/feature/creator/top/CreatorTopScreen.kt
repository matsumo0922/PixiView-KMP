package me.matsumo.fanbox.feature.creator.top

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import com.svenjacobs.reveal.Key
import com.svenjacobs.reveal.Reveal
import com.svenjacobs.reveal.RevealOverlayArrangement
import com.svenjacobs.reveal.RevealOverlayScope
import com.svenjacobs.reveal.RevealShape
import com.svenjacobs.reveal.RevealState
import com.svenjacobs.reveal.rememberRevealState
import com.svenjacobs.reveal.revealable
import com.svenjacobs.reveal.shapes.balloon.Arrow
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.model.TranslationState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_toast_require_plus
import me.matsumo.fanbox.core.resources.creator_tab_plans
import me.matsumo.fanbox.core.resources.creator_tab_posts
import me.matsumo.fanbox.core.resources.reveal_creator_top_fab
import me.matsumo.fanbox.core.resources.reveal_creator_top_search
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.component.CollapsingToolbarScaffold
import me.matsumo.fanbox.core.ui.component.ScrollStrategy
import me.matsumo.fanbox.core.ui.component.rememberCollapsingToolbarScaffoldState
import me.matsumo.fanbox.core.ui.extensition.LocalRevealCanvasState
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.OverlayText
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.revealByStep
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopDescriptionDialog
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopHeader
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopMenuDialog
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopPlansScreen
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopPostsScreen
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopRewardAdDialog
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopTopAppBar
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlan
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxTag
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

internal enum class CreatorTopRevealKeys {
    Search,
    Fab,
}

@Composable
internal fun CreatorTopRoute(
    isPosts: Boolean,
    navigateTo: (Destination) -> Unit,
    navigateToAlertDialog: (SimpleAlertContents, () -> Unit, () -> Unit) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatorTopViewModel = koinViewModel(),
    navigatorExtension: NavigatorExtension = koinInject(),
    toastExtension: ToastExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val scope = rememberCoroutineScope()

    val revealCanvasState = LocalRevealCanvasState.current
    val revealState = rememberRevealState()
    val revealOverlayContainerColor = MaterialTheme.colorScheme.tertiaryContainer
    val revealOverlayContentColor = MaterialTheme.colorScheme.onTertiaryContainer

    val snackbarHostState = LocalSnackbarHostState.current
    val requirePlus = stringResource(Res.string.billing_plus_toast_require_plus, appName)

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = viewModel::fetch,
        terminate = terminate,
    ) { uiState ->
        val creatorPostsPaging = uiState.creatorPostsPaging.collectAsLazyPagingItems()

        Reveal(
            modifier = Modifier.fillMaxSize(),
            onOverlayClick = { scope.launch { revealState.hide() } },
            revealCanvasState = revealCanvasState,
            revealState = revealState,
            overlayContent = { key -> RevealOverlayContent(key, revealOverlayContainerColor, revealOverlayContentColor) },
        ) {
            CreatorTopScreen(
                modifier = Modifier.fillMaxSize(),
                revealState = revealState,
                shouldShowReveal = uiState.shouldShowReveal,
                isPosts = isPosts,
                isBlocked = uiState.isBlocked,
                isAbleToReward = uiState.isAbleToReward,
                userData = uiState.userData,
                bookmarkedPostsIds = uiState.bookmarkedPostsIds.toImmutableList(),
                creatorDetail = uiState.creatorDetail,
                creatorPlans = uiState.creatorPlans.toImmutableList(),
                creatorTags = uiState.creatorTags.toImmutableList(),
                creatorPostsPaging = creatorPostsPaging,
                descriptionTransState = uiState.descriptionTransState,
                onClickSearch = { navigateTo(Destination.PostByCreatorSearch(it)) },
                onClickAllDownload = { navigateTo(Destination.CreatorPostsDownload(it)) },
                onClickPost = { navigateTo(Destination.PostDetail(it, Destination.PostDetail.PagingType.Creator)) },
                onClickPlan = { navigatorExtension.navigateToWebPage(it.planBrowserUrl, "") },
                onClickTag = { navigateTo(Destination.PostSearch(uiState.creatorDetail.creatorId, null, it.name)) },
                onTerminate = terminate,
                onClickLink = { navigatorExtension.navigateToWebPage(it, "") },
                onClickFollow = viewModel::follow,
                onClickUnfollow = viewModel::unfollow,
                onClickPostBookmark = viewModel::postBookmark,
                onClickBillingPlus = {
                    scope.launch { toastExtension.show(snackbarHostState, requirePlus) }
                    navigateTo(Destination.BillingPlusBottomSheet(it))
                },
                onShowBlockDialog = {
                    navigateToAlertDialog.invoke(
                        SimpleAlertContents.CreatorBlock,
                        {
                            scope.launch {
                                viewModel.blockCreator()
                                terminate.invoke()
                            }
                        },
                        { /* do nothing */ },
                    )
                },
                onShowUnblockDialog = {
                    navigateToAlertDialog.invoke(
                        SimpleAlertContents.CreatorUnblock,
                        {
                            scope.launch {
                                viewModel.unblockCreator()
                                viewModel.fetch()
                                creatorPostsPaging.refresh()
                            }
                        },
                        { terminate.invoke() },
                    )
                },
                onClickTranslateDescription = viewModel::translateDescription,
                onRevealCompleted = viewModel::finishReveal,
                onRewarded = viewModel::rewarded,
                onClickPostLike = viewModel::postLike,
            )
        }
    }
}

@OptIn(ExperimentalComposeUiApi::class)
@Composable
private fun CreatorTopScreen(
    revealState: RevealState,
    isPosts: Boolean,
    isBlocked: Boolean,
    isAbleToReward: Boolean,
    shouldShowReveal: Boolean,
    creatorDetail: FanboxCreatorDetail,
    userData: UserData,
    bookmarkedPostsIds: ImmutableList<FanboxPostId>,
    creatorPlans: ImmutableList<FanboxCreatorPlan>,
    creatorTags: ImmutableList<FanboxTag>,
    creatorPostsPaging: LazyPagingItems<FanboxPost>,
    descriptionTransState: TranslationState<String>,
    onClickSearch: (FanboxCreatorId) -> Unit,
    onClickAllDownload: (FanboxCreatorId) -> Unit,
    onClickBillingPlus: (String?) -> Unit,
    onClickPost: (FanboxPostId) -> Unit,
    onClickPostLike: (FanboxPostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickPlan: (FanboxCreatorPlan) -> Unit,
    onClickTag: (FanboxTag) -> Unit,
    onClickLink: (String) -> Unit,
    onClickFollow: suspend (FanboxUserId) -> Result<Unit>,
    onClickUnfollow: suspend (FanboxUserId) -> Result<Unit>,
    onShowBlockDialog: (SimpleAlertContents) -> Unit,
    onShowUnblockDialog: (SimpleAlertContents) -> Unit,
    onClickTranslateDescription: (String) -> Unit,
    onRevealCompleted: () -> Unit,
    onRewarded: () -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current

    val state = rememberCollapsingToolbarScaffoldState()
    val pagerState = rememberPagerState(initialPage = if (isPosts) 0 else 1) { 2 }
    val scope = rememberCoroutineScope()

    val insets = WindowInsets.statusBars.asPaddingValues()
    val topInset = insets.calculateTopPadding()

    val postsGridState = rememberLazyGridState()
    val plansListState = rememberLazyListState()

    var topAppBarHeight by remember { mutableStateOf(0.dp) }
    var isShowRewardAdDialog by remember { mutableStateOf(false) }
    var isShowDescriptionDialog by remember { mutableStateOf(false) }
    var isShowMenuDialog by remember { mutableStateOf(false) }
    var isVisibleFAB by remember { mutableStateOf(false) }

    val tabs = listOf(
        CreatorTab.POSTS,
        CreatorTab.PLANS,
    )

    BackHandler(revealState.isVisible) {
        // do nothing
    }

    LaunchedEffect(true) {
        isVisibleFAB = true
    }

    LaunchedEffect(isBlocked) {
        if (isBlocked) {
            delay(1000)
            onShowUnblockDialog.invoke(SimpleAlertContents.CreatorUnblock)
        }
    }

    if (shouldShowReveal) {
        LaunchedEffect(shouldShowReveal) {
            delay(500)
            revealState.revealByStep(
                keys = listOf(
                    CreatorTopRevealKeys.Search,
                    CreatorTopRevealKeys.Fab,
                ),
                onCompleted = onRevealCompleted,
            )
        }
    }

    Box(modifier) {
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            state = state,
            toolbarModifier = Modifier
                .heightIn(min = topInset + topAppBarHeight)
                .verticalScroll(rememberScrollState()),
            toolbar = {
                Spacer(
                    modifier = Modifier
                        .statusBarsPadding()
                        .fillMaxWidth(),
                )

                CreatorTopHeader(
                    modifier = Modifier
                        .parallax(1f)
                        .fillMaxWidth()
                        .graphicsLayer {
                            alpha = state.toolbarState.progress * 10
                        },
                    creatorDetail = creatorDetail,
                    onClickLink = onClickLink,
                    onClickFollow = onClickFollow,
                    onClickUnfollow = onClickUnfollow,
                    onClickSupporting = onClickLink,
                    onClickDescription = { isShowDescriptionDialog = true },
                )
            },
            scrollStrategy = ScrollStrategy.ExitUntilCollapsed,
        ) {
            Column(Modifier.fillMaxSize()) {
                TabRow(
                    modifier = Modifier.fillMaxWidth(),
                    selectedTabIndex = pagerState.currentPage,
                ) {
                    tabs.forEachIndexed { index, tab ->
                        CreatorTab(
                            isSelected = pagerState.currentPage == index,
                            label = stringResource(tab.titleRes),
                            onClick = {
                                scope.launch {
                                    if (pagerState.currentPage != index) {
                                        pagerState.animateScrollToPage(index)
                                    } else {
                                        when (tabs[index]) {
                                            CreatorTab.POSTS -> postsGridState.animateScrollToItem(0)
                                            CreatorTab.PLANS -> plansListState.animateScrollToItem(0)
                                        }
                                    }
                                }
                            },
                        )
                    }
                }

                HorizontalPager(
                    modifier = Modifier.fillMaxSize(),
                    state = pagerState,
                ) {
                    when (tabs[it]) {
                        CreatorTab.POSTS -> {
                            LazyPagingItemsLoadContents(
                                modifier = Modifier.fillMaxSize(),
                                lazyPagingItems = creatorPostsPaging,
                                isSwipeEnabled = state.toolbarState.progress == 1f,
                            ) {
                                CreatorTopPostsScreen(
                                    modifier = Modifier.fillMaxSize(),
                                    state = postsGridState,
                                    userData = userData,
                                    bookmarkedPostsIds = bookmarkedPostsIds.toImmutableList(),
                                    pagingAdapter = creatorPostsPaging,
                                    creatorTags = creatorTags,
                                    onClickPost = onClickPost,
                                    onClickPostLike = onClickPostLike,
                                    onClickPostBookmark = onClickPostBookmark,
                                    onClickTag = onClickTag,
                                    onClickCreator = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(1)
                                        }
                                    },
                                    onClickPlanList = {
                                        scope.launch {
                                            pagerState.animateScrollToPage(1)
                                        }
                                    },
                                )
                            }
                        }

                        CreatorTab.PLANS -> {
                            CreatorTopPlansScreen(
                                modifier = Modifier.fillMaxSize(),
                                state = plansListState,
                                creatorPlans = creatorPlans,
                                onClickPlan = onClickPlan,
                                onClickFanbox = { onClickLink.invoke("https://www.fanbox.cc/@${creatorDetail.creatorId}") },
                            )
                        }
                    }
                }
            }
        }

        CreatorTopTopAppBar(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .onGloballyPositioned {
                    topAppBarHeight = with(density) { it.size.height.toDp() }
                    Napier.d { "topAppBarHeight: $topAppBarHeight" }
                },
            title = creatorDetail.user?.name.orEmpty(),
            isShowTitle = state.toolbarState.progress == 0f,
            windowInsets = WindowInsets(0, 0, 0, 0),
            revealState = revealState,
            onClickNavigation = onTerminate,
            onClickActions = { isShowMenuDialog = true },
            onClickSearch = {
                if (userData.hasPrivilege) {
                    onClickSearch.invoke(creatorDetail.creatorId)
                } else {
                    onClickBillingPlus.invoke("search_post_by_creator")
                }
            },
        )

        AnimatedVisibility(
            modifier = Modifier
                .padding(16.dp)
                .navigationBarsPadding()
                .align(Alignment.BottomEnd),
            visible = isVisibleFAB,
            enter = fadeIn() + scaleIn(),
            exit = fadeOut() + scaleOut(),
        ) {
            FloatingActionButton(
                modifier = Modifier.revealable(
                    key = CreatorTopRevealKeys.Fab,
                    state = revealState,
                    shape = RevealShape.RoundRect(8.dp),
                ),
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    if (userData.hasPrivilege) {
                        onClickAllDownload.invoke(creatorDetail.creatorId)
                    } else {
                        isShowRewardAdDialog = true
                    }
                },
            ) {
                Icon(
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                )
            }
        }
    }

    if (isShowRewardAdDialog) {
        CreatorTopRewardAdDialog(
            isAbleToReward = isAbleToReward,
            onRewarded = {
                onRewarded.invoke()
                onClickAllDownload.invoke(creatorDetail.creatorId)
                isShowRewardAdDialog = false
            },
            onClickShowPlus = {
                onClickBillingPlus.invoke("all_download")
                isShowRewardAdDialog = false
            },
            onDismissRequest = { isShowRewardAdDialog = false },
        )
    }

    if (isShowMenuDialog) {
        CreatorTopMenuDialog(
            isVisible = isShowMenuDialog,
            onClickBlock = { onShowBlockDialog.invoke(SimpleAlertContents.CreatorBlock) },
            onDismissRequest = { isShowMenuDialog = false },
        )
    }

    if (isShowDescriptionDialog) {
        CreatorTopDescriptionDialog(
            description = creatorDetail.description,
            translationState = descriptionTransState,
            onTranslateClicked = {
                if (userData.hasPrivilege) {
                    onClickTranslateDescription.invoke(it)
                } else {
                    onClickBillingPlus.invoke("translate")
                    isShowDescriptionDialog = false
                }
            },
            onDismissRequest = { isShowDescriptionDialog = false },
        )
    }
}

@Composable
private fun CreatorTab(
    isSelected: Boolean,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Tab(
        modifier = modifier,
        text = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium.copy(fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal),
                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
            )
        },
        selected = isSelected,
        onClick = onClick,
    )
}

@Composable
private fun RevealOverlayScope.RevealOverlayContent(
    key: Key,
    containerColor: Color,
    contentColor: Color,
) {
    when (key) {
        CreatorTopRevealKeys.Search -> {
            OverlayText(
                modifier = Modifier.align(
                    horizontalAlignment = Alignment.End,
                    verticalArrangement = RevealOverlayArrangement.Bottom,
                ),
                text = stringResource(Res.string.reveal_creator_top_search, appName),
                arrow = Arrow.top(horizontalAlignment = Alignment.End),
                containerColor = containerColor,
                contentColor = contentColor,
            )
        }

        CreatorTopRevealKeys.Fab -> {
            OverlayText(
                modifier = Modifier.align(
                    horizontalArrangement = RevealOverlayArrangement.Start,
                ),
                text = stringResource(Res.string.reveal_creator_top_fab, appName),
                arrow = Arrow.end(),
                containerColor = containerColor,
                contentColor = contentColor,
            )
        }
    }
}

private enum class CreatorTab(val titleRes: StringResource) {
    POSTS(Res.string.creator_tab_posts),
    PLANS(Res.string.creator_tab_plans),
}
