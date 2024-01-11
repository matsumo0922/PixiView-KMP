package me.matsumo.fanbox.feature.library.home

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import app.cash.paging.compose.collectAsLazyPagingItems
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.library.home.items.LibraryHomeIdleSection
import me.matsumo.fanbox.feature.library.home.items.LibrarySupportedIdleSection
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
internal fun LibraryHomeScreen(
    openDrawer: () -> Unit,
    navigateToPostDetailFromHome: (PostId) -> Unit,
    navigateToPostDetailFromSupported: (PostId) -> Unit,
    navigateToCreatorPlans: (CreatorId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToCancelPlus: (SimpleAlertContents) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryHomeViewModel = koinViewModel(LibraryHomeViewModel::class),
) {
    val navigationType = LocalNavigationType.current.type
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val homePager = uiState.homePaging.collectAsLazyPagingItems()
    val supportedPager = uiState.supportedPaging.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null, flingAnimationSpec = null)

    val pagerState = rememberPagerState(initialPage = if (uiState.userData.isFollowTabDefaultHome) 1 else 0) { 2 }
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        HomeTabs.Supported,
        HomeTabs.Home,
    )

    LaunchedEffect(true) {
        viewModel.cancelPlusTrigger.collectLatest {
            navigateToCancelPlus.invoke(SimpleAlertContents.CancelPlus)
        }
    }

    Scaffold(
        modifier = if (navigationType != PixiViewNavigationType.PermanentNavigationDrawer) modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else modifier,
        topBar = {
            if (navigationType != PixiViewNavigationType.PermanentNavigationDrawer) {
                CenterAlignedTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(text = stringResource(MR.strings.about_name))
                    },
                    navigationIcon = {
                        IconButton(onClick = openDrawer) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = null,
                            )
                        }
                    },
                    scrollBehavior = scrollBehavior,
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                        containerColor = MaterialTheme.colorScheme.surface,
                        scrolledContainerColor = MaterialTheme.colorScheme.surface,
                    ),
                )
            }
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Column(Modifier.padding(padding)) {
            TabRow(
                modifier = Modifier.fillMaxWidth(),
                selectedTabIndex = pagerState.currentPage,
            ) {
                tabs.forEachIndexed { index, tab ->
                    HomeTab(
                        isSelected = pagerState.currentPage == index,
                        label = stringResource(tab.titleRes),
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(index)
                            }
                        },
                    )
                }
            }

            HorizontalPager(pagerState) {
                when (tabs[it]) {
                    HomeTabs.Home -> {
                        LazyPagingItemsLoadContents(
                            modifier = Modifier.fillMaxSize(),
                            lazyPagingItems = homePager,
                            emptyMessageRes = MR.strings.error_no_data_following,
                        ) {
                            LibraryHomeIdleSection(
                                modifier = Modifier.fillMaxSize(),
                                pagingAdapter = homePager,
                                userData = uiState.userData,
                                // nativeAdsPreLoader = viewModel.adsPreLoader,
                                nativeAdUnitId = uiState.nativeAdUnitId,
                                bookmarkedPosts = uiState.bookmarkedPosts.toImmutableList(),
                                onClickPost = navigateToPostDetailFromHome,
                                onClickPostLike = viewModel::postLike,
                                onClickPostBookmark = viewModel::postBookmark,
                                onClickCreator = navigateToCreatorPosts,
                                onClickPlanList = navigateToCreatorPlans,
                            )
                        }
                    }
                    HomeTabs.Supported -> {
                        LazyPagingItemsLoadContents(
                            modifier = Modifier.fillMaxSize(),
                            lazyPagingItems = supportedPager,
                            emptyMessageRes = MR.strings.error_no_data_supported,
                        ) {
                            LibrarySupportedIdleSection(
                                modifier = Modifier.fillMaxSize(),
                                pagingAdapter = supportedPager,
                                userData = uiState.userData,
                                // nativeAdsPreLoader = viewModel.adsPreLoader,
                                nativeAdUnitId = uiState.nativeAdUnitId,
                                bookmarkedPosts = uiState.bookmarkedPosts.toImmutableList(),
                                onClickPost = navigateToPostDetailFromSupported,
                                onClickPostLike = viewModel::postLike,
                                onClickPostBookmark = viewModel::postBookmark,
                                onClickCreator = navigateToCreatorPosts,
                                onClickPlanList = navigateToCreatorPlans,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeTab(
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

private enum class HomeTabs(val titleRes: StringResource) {
    Home(MR.strings.home_tab_home),
    Supported(MR.strings.home_tab_supported),
}
