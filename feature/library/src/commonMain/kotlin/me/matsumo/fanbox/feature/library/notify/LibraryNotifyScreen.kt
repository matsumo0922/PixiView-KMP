package me.matsumo.fanbox.feature.library.notify

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import app.cash.paging.compose.itemContentType
import app.cash.paging.compose.itemKey
import me.matsumo.fanbox.core.model.fanbox.FanboxBell
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_no_data_notify
import me.matsumo.fanbox.core.resources.library_navigation_notify
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.view.PagingErrorSection
import me.matsumo.fanbox.feature.library.notify.items.LibraryNotifyBellItem
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun LibraryNotifyRoute(
    openDrawer: () -> Unit,
    navigateToPostDetail: (PostId) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: LibraryNotifyViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) {
        val paging = it.paging.collectAsLazyPagingItems()

        LibraryNotifyScreen(
            modifier = Modifier.fillMaxSize(),
            openDrawer = openDrawer,
            onClickBell = navigateToPostDetail,
            pagingAdapter = paging,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LibraryNotifyScreen(
    pagingAdapter: LazyPagingItems<FanboxBell>,
    openDrawer: () -> Unit,
    onClickBell: (PostId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigationType = LocalNavigationType.current.type
    val state = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.library_navigation_notify),
                navigationIcon = Icons.Default.Menu,
                onClickNavigation = if (navigationType != PixiViewNavigationType.PermanentNavigationDrawer) openDrawer else null,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            HorizontalDivider()
        },
    ) { padding ->
        LazyPagingItemsLoadContents(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            lazyPagingItems = pagingAdapter,
            emptyMessageRes = Res.string.error_no_data_notify,
        ) {
            LazyColumn(
                modifier = Modifier.drawVerticalScrollbar(state),
                state = state,
            ) {
                items(
                    count = pagingAdapter.itemCount,
                    key = pagingAdapter.itemKey {
                        when (it) {
                            is FanboxBell.Comment -> "comment-${it.id}"
                            is FanboxBell.Like -> "like-${it.id}"
                            is FanboxBell.PostPublished -> "post-${it.id}"
                        }
                    },
                    contentType = pagingAdapter.itemContentType(),
                ) { index ->
                    pagingAdapter[index]?.let { bell ->
                        LibraryNotifyBellItem(
                            modifier = Modifier.fillMaxWidth(),
                            bell = bell,
                            onClickBell = onClickBell,
                        )

                        HorizontalDivider()
                    }
                }

                if (pagingAdapter.loadState.append is LoadState.Error) {
                    item {
                        PagingErrorSection(
                            modifier = Modifier.fillMaxWidth(),
                            onRetry = { pagingAdapter.retry() },
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.navigationBarsPadding())
                }
            }
        }
    }
}
