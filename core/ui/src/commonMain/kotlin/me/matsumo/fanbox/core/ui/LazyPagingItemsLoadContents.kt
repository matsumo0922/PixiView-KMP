package me.matsumo.fanbox.core.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.paging.CombinedLoadStates
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_executed
import me.matsumo.fanbox.core.resources.error_no_data
import me.matsumo.fanbox.core.ui.extensition.isEmpty
import me.matsumo.fanbox.core.ui.view.EmptyView
import me.matsumo.fanbox.core.ui.view.ErrorView
import me.matsumo.fanbox.core.ui.view.LoadingView
import me.matsumo.fanbox.core.ui.view.PullToRefreshWrapper
import org.jetbrains.compose.resources.StringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Any> LazyPagingItemsLoadContents(
    lazyPagingItems: LazyPagingItems<T>,
    modifier: Modifier = Modifier,
    isSwipeEnabled: Boolean = true,
    emptyTitleRes: StringResource = Res.string.error_no_data,
    emptyMessageRes: StringResource = Res.string.error_executed,
    content: @Composable (CombinedLoadStates) -> Unit,
) {
    Surface(modifier) {
        if (lazyPagingItems.isEmpty()) {
            EmptyView(
                titleRes = emptyTitleRes,
                messageRes = emptyMessageRes,
            )
        } else {
            lazyPagingItems.apply {
                AnimatedContent(
                    targetState = loadState.refresh,
                    transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                    contentKey = { it::class },
                    label = "LazyPagingItemsLoadContents",
                ) {
                    when (it) {
                        is LoadState.Loading -> {
                            LoadingView()
                        }

                        is LoadState.Error -> {
                            ErrorView(
                                errorState = ScreenState.Error(Res.string.error_no_data),
                                retryAction = { lazyPagingItems.refresh() },
                                terminate = null,
                            )
                        }

                        is LoadState.NotLoading -> {
                            val isRefreshing by remember(lazyPagingItems.loadState) {
                                derivedStateOf { lazyPagingItems.loadState.refresh is LoadState.Loading }
                            }

                            PullToRefreshWrapper(
                                onRefresh = { lazyPagingItems.refresh() },
                                isRefreshing = isRefreshing,
                                enabled = isSwipeEnabled,
                            ) {
                                content.invoke(lazyPagingItems.loadState)
                            }
                        }
                    }
                }
            }
        }
    }
}
