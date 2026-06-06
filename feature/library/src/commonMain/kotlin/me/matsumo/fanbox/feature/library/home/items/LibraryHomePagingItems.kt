package me.matsumo.fanbox.feature.library.home.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.ui.ads.NativeAdView
import me.matsumo.fanbox.core.ui.component.PostGridItem
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.view.PagingErrorSection
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

@Composable
internal fun LibraryHomePagingItems(
    state: LazyGridState,
    columns: Int,
    adOffset: Int,
    adInterval: Int,
    pagingAdapter: LazyPagingItems<FanboxPost>,
    setting: Setting,
    bookmarkedPostsIds: ImmutableList<FanboxPostId>,
    isGridMode: Boolean,
    onClickPost: (FanboxPostId) -> Unit,
    onClickPostLike: (FanboxPostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (FanboxCreatorId) -> Unit,
    onClickPlanList: (FanboxCreatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyVerticalGrid(
        modifier = modifier.drawVerticalScrollbar(state, columns),
        state = state,
        columns = GridCells.Fixed(columns),
        contentPadding = PaddingValues(if (isGridMode) 0.dp else 16.dp),
        horizontalArrangement = Arrangement.spacedBy(if (isGridMode) 4.dp else 16.dp),
        verticalArrangement = Arrangement.spacedBy(if (isGridMode) 4.dp else 16.dp),
    ) {
        items(
            count = pagingAdapter.itemCount + if (setting.hasPrivilege) 0 else (pagingAdapter.itemCount / adInterval),
            key = { index ->
                when {
                    setting.hasPrivilege -> pagingAdapter.itemKey { it.id.uniqueValue }(index)
                    (index + adOffset) % adInterval == 0 -> "ad-$index"
                    else -> pagingAdapter.itemKey { it.id.uniqueValue }(index - ((index + adOffset) / adInterval))
                }
            },
        ) { index ->
            if ((index + adOffset) % adInterval == 0 && !setting.hasPrivilege) {
                NativeAdView(
                    modifier = Modifier.fillMaxSize(),
                    key = "$index",
                )
            } else {
                pagingAdapter[if (setting.hasPrivilege) index else index - ((index + adOffset) / adInterval)]?.let { post ->
                    if (isGridMode) {
                        PostGridItem(
                            modifier = Modifier.fillMaxWidth(),
                            post = post,
                            isHideAdultContents = setting.isHideAdultContents,
                            isOverrideAdultContents = setting.isAllowedShowAdultContents,
                            onClickPost = onClickPost,
                        )
                    } else {
                        PostItem(
                            modifier = Modifier.fillMaxSize(),
                            post = post,
                            isBookmarked = bookmarkedPostsIds.contains(post.id),
                            isHideAdultContents = setting.isHideAdultContents,
                            isOverrideAdultContents = setting.isAllowedShowAdultContents,
                            isTestUser = setting.isTestUser,
                            onClickPost = onClickPost,
                            onClickCreator = onClickCreator,
                            onClickPlanList = onClickPlanList,
                            onClickLike = onClickPostLike,
                            onClickBookmark = { _, isBookmarked ->
                                onClickPostBookmark.invoke(post, isBookmarked)
                            },
                        )
                    }
                }
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
