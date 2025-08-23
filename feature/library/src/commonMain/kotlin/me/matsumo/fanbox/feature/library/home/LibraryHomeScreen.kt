package me.matsumo.fanbox.feature.library.home

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
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_no_data_following
import me.matsumo.fanbox.core.resources.error_no_data_supported
import me.matsumo.fanbox.core.resources.home_tab_home
import me.matsumo.fanbox.core.resources.home_tab_supported
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.feature.library.home.items.LibraryHomeIdleSection
import me.matsumo.fanbox.feature.library.home.items.LibrarySupportedIdleSection
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun LibraryHomeScreen(
    openDrawer: () -> Unit,
    navigateTo: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryHomeViewModel = koinViewModel(),
) {
    val navigationType = LocalNavigationType.current.type
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val homePager = uiState.homePaging.collectAsLazyPagingItems()
    val supportedPager = uiState.supportedPaging.collectAsLazyPagingItems()
    val scrollBehavior = TopAppBarDefaults.enterAlwaysScrollBehavior(snapAnimationSpec = null, flingAnimationSpec = null)

    val pagerState = rememberPagerState(initialPage = if (uiState.setting.isDefaultFollowTabInHome) 1 else 0) { 2 }
    val scope = rememberCoroutineScope()

    val tabs = listOf(
        HomeTabs.Supported,
        HomeTabs.Home,
    )

    LaunchedEffect(true) {
        viewModel.updatePlusTrigger.collectLatest {
            if (!it) {
                val content = SimpleAlertContents.CancelPlus
                val destination = Destination.SimpleAlertDialog(content)

                navigateTo(destination)
            }
        }
    }

    LaunchedEffect(uiState.setting.isDefaultFollowTabInHome) {
        pagerState.scrollToPage(if (uiState.setting.isDefaultFollowTabInHome) 1 else 0)
    }

    Scaffold(
        modifier = if (navigationType != PixiViewNavigationType.PermanentNavigationDrawer) modifier.nestedScroll(scrollBehavior.nestedScrollConnection) else modifier,
        topBar = {
            if (navigationType != PixiViewNavigationType.PermanentNavigationDrawer) {
                CenterAlignedTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(text = appName)
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

            HorizontalPager(pagerState) { index ->
                when (tabs[index]) {
                    HomeTabs.Home -> {
                        LazyPagingItemsLoadContents(
                            modifier = Modifier.fillMaxSize(),
                            lazyPagingItems = homePager,
                            emptyMessageRes = Res.string.error_no_data_following,
                        ) {
                            LibraryHomeIdleSection(
                                modifier = Modifier.fillMaxSize(),
                                pagingAdapter = homePager,
                                setting = uiState.setting,
                                bookmarkedPostsIds = uiState.bookmarkedPostsIds.toImmutableList(),
                                onClickPost = { navigateTo(Destination.PostDetail(it, Destination.PostDetail.PagingType.Home)) },
                                onClickPostLike = viewModel::postLike,
                                onClickPostBookmark = viewModel::postBookmark,
                                onClickCreator = { navigateTo(Destination.CreatorTop(it, true)) },
                                onClickPlanList = { navigateTo(Destination.CreatorTop(it, false)) },
                            )
                        }
                    }
                    HomeTabs.Supported -> {
                        LazyPagingItemsLoadContents(
                            modifier = Modifier.fillMaxSize(),
                            lazyPagingItems = supportedPager,
                            emptyMessageRes = Res.string.error_no_data_supported,
                        ) {
                            LibrarySupportedIdleSection(
                                modifier = Modifier.fillMaxSize(),
                                pagingAdapter = supportedPager,
                                setting = uiState.setting,
                                bookmarkedPostsIds = uiState.bookmarkedPostsIds.toImmutableList(),
                                onClickPost = { navigateTo(Destination.PostDetail(it, Destination.PostDetail.PagingType.Supported)) },
                                onClickPostLike = viewModel::postLike,
                                onClickPostBookmark = viewModel::postBookmark,
                                onClickCreator = { navigateTo(Destination.CreatorTop(it, true)) },
                                onClickPlanList = { navigateTo(Destination.CreatorTop(it, false)) },
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
    Home(Res.string.home_tab_home),
    Supported(Res.string.home_tab_supported),
}
