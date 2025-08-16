package me.matsumo.fanbox.feature.library.discovery

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_toast_require_plus
import me.matsumo.fanbox.core.resources.creator_following_pixiv
import me.matsumo.fanbox.core.resources.creator_recommended
import me.matsumo.fanbox.core.resources.library_navigation_discovery
import me.matsumo.fanbox.core.resources.search_post_by_creator
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.component.CreatorItem
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.feature.library.discovery.components.LibraryDiscoverySearchPostCreatorItem
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LibraryDiscoveryRoute(
    openDrawer: () -> Unit,
    navigateTo: (Destination) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryDiscoveryViewModel = koinViewModel(),
    navigatorExtension: NavigatorExtension = koinInject(),
    toastExtension: ToastExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val requirePlus = stringResource(Res.string.billing_plus_toast_require_plus, appName)

    LaunchedEffect(true) {
        if (screenState !is ScreenState.Idle) {
            viewModel.fetch()
        }
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = viewModel::fetch,
    ) { uiState ->
        LibraryDiscoveryScreen(
            modifier = Modifier.fillMaxSize(),
            setting = uiState.setting,
            followingCreators = uiState.followingCreators.toImmutableList(),
            recommendedCreators = uiState.recommendedCreators.toImmutableList(),
            followingPixivCreators = uiState.followingPixivCreators.toImmutableList(),
            openDrawer = openDrawer,
            fetch = viewModel::fetch,
            onClickSearch = { navigateTo(Destination.PostSearch(FanboxCreatorId(""), null, null)) },
            onClickPostByCreatorSearch = { navigateTo(Destination.PostByCreatorSearch(it)) },
            onClickCreator = { navigateTo(Destination.CreatorTop(it, true)) },
            onClickFollow = viewModel::follow,
            onClickUnfollow = viewModel::unfollow,
            onClickSupporting = { navigatorExtension.navigateToWebPage(it, LibraryDiscoveryRoute) },
            onClickBillingPlus = {
                scope.launch { toastExtension.show(snackbarHostState, requirePlus) }
                navigateTo(Destination.BillingPlusBottomSheet(it))
            },
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryDiscoveryScreen(
    setting: Setting,
    followingCreators: ImmutableList<FanboxCreatorDetail>,
    recommendedCreators: ImmutableList<FanboxCreatorDetail>,
    followingPixivCreators: ImmutableList<FanboxCreatorDetail>,
    openDrawer: () -> Unit,
    fetch: () -> Unit,
    onClickSearch: () -> Unit,
    onClickPostByCreatorSearch: (FanboxCreatorId) -> Unit,
    onClickCreator: (FanboxCreatorId) -> Unit,
    onClickFollow: suspend (FanboxUserId) -> Result<Unit>,
    onClickUnfollow: suspend (FanboxUserId) -> Result<Unit>,
    onClickSupporting: (String) -> Unit,
    onClickBillingPlus: (String?) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationType = LocalNavigationType.current.type
    val scope = rememberCoroutineScope()
    val state = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val columns = when (LocalNavigationType.current.type) {
        PixiViewNavigationType.BottomNavigation -> 1
        PixiViewNavigationType.NavigationRail -> 2
        PixiViewNavigationType.PermanentNavigationDrawer -> 2
        else -> 1
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.library_navigation_discovery),
                navigationIcon = Icons.Default.Menu,
                actionsIcon = Icons.Default.Search,
                onClickNavigation = if (navigationType != PixiViewNavigationType.PermanentNavigationDrawer) openDrawer else null,
                onClickActions = onClickSearch,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            HorizontalDivider()
        },
    ) { padding ->
        PullToRefreshBox(
            modifier = Modifier.padding(padding),
            onRefresh = fetch,
            isRefreshing = false,
        ) {
            LazyVerticalGrid(
                modifier = Modifier.drawVerticalScrollbar(state, columns),
                state = state,
                columns = GridCells.Fixed(columns),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                if (followingCreators.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        TitleItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(Res.string.search_post_by_creator),
                        )
                    }

                    items(
                        items = followingCreators.take(5),
                        key = { item -> "search-${item.creatorId.value}" },
                        span = { GridItemSpan(maxLineSpan) },
                    ) { creatorDetail ->
                        LibraryDiscoverySearchPostCreatorItem(
                            modifier = Modifier.fillMaxWidth(),
                            creatorDetail = creatorDetail,
                            onSearchPostClicked = {
                                if (setting.hasPrivilege) {
                                    onClickPostByCreatorSearch.invoke(it)
                                } else {
                                    onClickBillingPlus.invoke("search_post_by_creator")
                                }
                            },
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (followingPixivCreators.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        TitleItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(Res.string.creator_following_pixiv),
                        )
                    }

                    items(
                        items = followingPixivCreators.take(6),
                        key = { item -> "pixiv-${item.creatorId.value}" },
                    ) {
                        var isFollowed by rememberSaveable { mutableStateOf(it.isFollowed) }

                        CreatorItem(
                            modifier = Modifier.fillMaxWidth(),
                            creatorDetail = it,
                            isFollowed = isFollowed,
                            onClickCreator = onClickCreator,
                            onClickFollow = {
                                scope.launch {
                                    isFollowed = true
                                    isFollowed = onClickFollow.invoke(it).isSuccess
                                }
                            },
                            onClickUnfollow = {
                                scope.launch {
                                    isFollowed = false
                                    isFollowed = !onClickUnfollow.invoke(it).isSuccess
                                }
                            },
                            onClickSupporting = onClickSupporting,
                        )
                    }

                    item {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                if (recommendedCreators.isNotEmpty()) {
                    item(span = { GridItemSpan(maxLineSpan) }) {
                        TitleItem(
                            modifier = Modifier.fillMaxWidth(),
                            title = stringResource(Res.string.creator_recommended),
                        )
                    }

                    items(
                        items = recommendedCreators,
                        key = { item -> "recommended-${item.creatorId.value}" },
                    ) {
                        var isFollowed by rememberSaveable { mutableStateOf(it.isFollowed) }

                        CreatorItem(
                            modifier = Modifier.fillMaxWidth(),
                            creatorDetail = it,
                            isFollowed = isFollowed,
                            onClickCreator = onClickCreator,
                            onClickFollow = {
                                scope.launch {
                                    isFollowed = true
                                    isFollowed = onClickFollow.invoke(it).isSuccess
                                }
                            },
                            onClickUnfollow = {
                                scope.launch {
                                    isFollowed = false
                                    isFollowed = !onClickUnfollow.invoke(it).isSuccess
                                }
                            },
                            onClickSupporting = onClickSupporting,
                        )
                    }
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}

@Composable
private fun TitleItem(
    title: String,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier,
        text = title.uppercase(),
        style = MaterialTheme.typography.bodyMedium.bold(),
        color = MaterialTheme.colorScheme.primary,
    )
}
