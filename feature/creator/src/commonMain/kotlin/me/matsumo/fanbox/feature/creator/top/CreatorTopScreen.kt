package me.matsumo.fanbox.feature.creator.top

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import caios.android.fanbox.feature.creator.top.items.CreatorTopPostsScreen
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorPlan
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorTag
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.CollapsingToolbarScaffold
import me.matsumo.fanbox.core.ui.component.ScrollStrategy
import me.matsumo.fanbox.core.ui.component.rememberCollapsingToolbarScaffoldState
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopDescriptionDialog
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopHeader
import me.matsumo.fanbox.feature.creator.top.items.CreatorTopPlansScreen
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CreatorTopRoute(
    creatorId: CreatorId,
    isPosts: Boolean,
    navigateToPostDetail: (PostId) -> Unit,
    navigateToPostSearch: (String, CreatorId) -> Unit,
    navigateToDownloadAll: (CreatorId) -> Unit,
    navigateToBillingPlus: () -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatorTopViewModel = koinViewModel(CreatorTopViewModel::class),
    navigatorExtension: NavigatorExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    LaunchedEffect(creatorId) {
        if (screenState !is ScreenState.Idle) {
            viewModel.fetch(creatorId)
        }
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { viewModel.fetch(creatorId) },
    ) { uiState ->
        CreatorTopScreen(
            modifier = Modifier.fillMaxSize(),
            isPosts = isPosts,
            userData = uiState.userData,
            nativeAdUnitId = uiState.nativeAdUnitId,
            // nativeAdsPreLoader = viewModel.adsPreLoader,
            bookmarkedPosts = uiState.bookmarkedPosts.toImmutableList(),
            creatorDetail = uiState.creatorDetail,
            creatorPlans = uiState.creatorPlans.toImmutableList(),
            creatorTags = uiState.creatorTags.toImmutableList(),
            creatorPostsPaging = uiState.creatorPostsPaging.collectAsLazyPagingItems(),
            onClickAllDownload = navigateToDownloadAll,
            onClickBillingPlus = navigateToBillingPlus,
            onClickPost = navigateToPostDetail,
            onClickPlan = { navigatorExtension.navigateToWebPage(it.planBrowserUrl) },
            onClickTag = { navigateToPostSearch.invoke(it.name, uiState.creatorDetail.creatorId) },
            onTerminate = terminate,
            onClickLink = navigatorExtension::navigateToWebPage,
            onClickFollow = viewModel::follow,
            onClickUnfollow = viewModel::unfollow,
            onClickSupporting = navigatorExtension::navigateToWebPage,
            onClickPostBookmark = viewModel::postBookmark,
            onClickPostLike = viewModel::postLike,
        )
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun CreatorTopScreen(
    isPosts: Boolean,
    creatorDetail: FanboxCreatorDetail,
    userData: UserData,
    nativeAdUnitId: String,
    // nativeAdsPreLoader: NativeAdsPreLoader,
    bookmarkedPosts: ImmutableList<PostId>,
    creatorPlans: ImmutableList<FanboxCreatorPlan>,
    creatorTags: ImmutableList<FanboxCreatorTag>,
    creatorPostsPaging: LazyPagingItems<FanboxPost>,
    onClickAllDownload: (CreatorId) -> Unit,
    onClickBillingPlus: () -> Unit,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickPlan: (FanboxCreatorPlan) -> Unit,
    onClickTag: (FanboxCreatorTag) -> Unit,
    onClickLink: (String) -> Unit,
    onClickFollow: suspend (String) -> Result<Unit>,
    onClickUnfollow: suspend (String) -> Result<Unit>,
    onClickSupporting: (String) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberCollapsingToolbarScaffoldState()
    val pagerState = rememberPagerState(initialPage = if (isPosts) 0 else 1) { 2 }
    val scope = rememberCoroutineScope()

    val postsListState = rememberLazyListState()
    val postsGridState = rememberLazyGridState()
    val plansListState = rememberLazyListState()

    var isShowDescriptionDialog by remember { mutableStateOf(false) }
    var isVisibleFAB by remember { mutableStateOf(false) }

    val tabs = listOf(
        CreatorTab.POSTS,
        CreatorTab.PLANS,
    )

    LaunchedEffect(true) {
        isVisibleFAB = true
    }

    Box(modifier) {
        CollapsingToolbarScaffold(
            modifier = Modifier.fillMaxSize(),
            state = state,
            toolbarModifier = Modifier.verticalScroll(rememberScrollState()),
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
                    onClickTerminate = onTerminate,
                    onClickMenu = {},
                    onClickLink = onClickLink,
                    onClickFollow = onClickFollow,
                    onClickUnfollow = onClickUnfollow,
                    onClickSupporting = onClickSupporting,
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
                                            CreatorTab.POSTS -> {
                                                postsListState.animateScrollToItem(0)
                                                postsGridState.animateScrollToItem(0)
                                            }
                                            CreatorTab.PLANS -> {
                                                plansListState.animateScrollToItem(0)
                                            }
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
                                    listState = postsListState,
                                    gridState = postsGridState,
                                    userData = userData,
                                    nativeAdUnitId = nativeAdUnitId,
                                    // nativeAdsPreLoader = nativeAdsPreLoader,
                                    bookmarkedPosts = bookmarkedPosts.toImmutableList(),
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
                            )
                        }
                    }
                }
            }
        }

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
                containerColor = MaterialTheme.colorScheme.primaryContainer,
                onClick = {
                    if (userData.hasPrivilege) {
                        onClickAllDownload.invoke(creatorDetail.creatorId)
                    } else {
                        // ToastUtil.show(context, R.string.creator_download_all_error)
                        onClickBillingPlus.invoke()
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

    if (isShowDescriptionDialog) {
        CreatorTopDescriptionDialog(
            description = creatorDetail.description,
            onLinkClick = onClickLink,
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

private enum class CreatorTab(val titleRes: StringResource) {
    POSTS(MR.strings.creator_tab_posts),
    PLANS(MR.strings.creator_tab_plans),
}
