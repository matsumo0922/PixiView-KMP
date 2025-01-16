package me.matsumo.fanbox.feature.post.search.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemContentType
import app.cash.paging.compose.itemKey
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_creator
import me.matsumo.fanbox.core.resources.common_tag
import me.matsumo.fanbox.core.resources.error_no_data_search
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.component.CreatorItem
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.view.PagingErrorSection
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxTag
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PostSearchCreatorScreen(
    pagingAdapter: LazyPagingItems<FanboxCreatorDetail>,
    suggestTags: ImmutableList<FanboxTag>,
    onClickCreator: (FanboxCreatorId) -> Unit,
    onClickTag: (String) -> Unit,
    onClickFollow: suspend (FanboxUserId) -> Result<Unit>,
    onClickUnfollow: suspend (FanboxUserId) -> Result<Unit>,
    onClickSupporting: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val scope = rememberCoroutineScope()
    val state = rememberLazyGridState()

    val columns = when (LocalNavigationType.current.type) {
        PixiViewNavigationType.BottomNavigation -> 1
        PixiViewNavigationType.NavigationRail -> 2
        PixiViewNavigationType.PermanentNavigationDrawer -> 2
        else -> 1
    }

    LaunchedEffect(state) {
        snapshotFlow { state.layoutInfo }.collect {
            keyboardController?.hide()
        }
    }

    LazyPagingItemsLoadContents(
        modifier = modifier,
        lazyPagingItems = pagingAdapter,
        emptyMessageRes = Res.string.error_no_data_search,
    ) {
        LazyVerticalGrid(
            modifier = Modifier.drawVerticalScrollbar(state, columns),
            state = state,
            columns = GridCells.Fixed(columns),
            contentPadding = PaddingValues(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            if (suggestTags.isNotEmpty()) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    TitleItem(
                        modifier = Modifier.fillMaxWidth(),
                        title = stringResource(Res.string.common_tag),
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    PostSearchSuggestTagsSection(
                        modifier = Modifier.fillMaxWidth(),
                        suggestTags = suggestTags,
                        onClickTag = onClickTag,
                    )
                }

                item(span = { GridItemSpan(maxLineSpan) }) {
                    TitleItem(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .fillMaxWidth(),
                        title = stringResource(Res.string.common_creator),
                    )
                }
            }

            items(
                count = pagingAdapter.itemCount,
                key = pagingAdapter.itemKey(),
                contentType = pagingAdapter.itemContentType(),
            ) { index ->
                pagingAdapter[index]?.let { creatorDetail ->
                    var isFollowed by rememberSaveable { mutableStateOf(creatorDetail.isFollowed) }

                    CreatorItem(
                        modifier = Modifier.fillMaxWidth(),
                        creatorDetail = creatorDetail,
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

            if (pagingAdapter.loadState.append is LoadState.Error) {
                item(span = { GridItemSpan(maxLineSpan) }) {
                    PagingErrorSection(
                        modifier = Modifier.fillMaxWidth(),
                        onRetry = { pagingAdapter.retry() },
                    )
                }
            }

            item(span = { GridItemSpan(maxLineSpan) }) {
                Spacer(modifier = Modifier.navigationBarsPadding())
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
