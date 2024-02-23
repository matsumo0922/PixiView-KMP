package me.matsumo.fanbox.feature.library.home.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemContentType
import app.cash.paging.compose.itemKey
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.ads.BannerAdView
import me.matsumo.fanbox.core.ui.ads.NativeAdView
import me.matsumo.fanbox.core.ui.component.PostGridItem
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.view.PagingErrorSection

@Composable
internal fun LibraryHomeIdleSection(
    pagingAdapter: LazyPagingItems<FanboxPost>,
    userData: UserData,
    bookmarkedPosts: ImmutableList<PostId>,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickPlanList: (CreatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    if (userData.isUseGridMode) {
        GridSection(
            pagingAdapter = pagingAdapter,
            userData = userData,
            bookmarkedPosts = bookmarkedPosts,
            onClickPost = onClickPost,
            modifier = modifier,
        )
    } else {
        ColumnSection(
            pagingAdapter = pagingAdapter,
            userData = userData,
            bookmarkedPosts = bookmarkedPosts,
            onClickPost = onClickPost,
            onClickPostLike = onClickPostLike,
            onClickPostBookmark = onClickPostBookmark,
            onClickCreator = onClickCreator,
            onClickPlanList = onClickPlanList,
            modifier = modifier,
        )
    }
}

@Composable
private fun ColumnSection(
    pagingAdapter: LazyPagingItems<FanboxPost>,
    userData: UserData,
    bookmarkedPosts: ImmutableList<PostId>,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickPlanList: (CreatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()

    val adOffset: Int
    val adInterval: Int

    if (currentPlatform == Platform.Android) {
        adOffset = 3
        adInterval = 4
    } else {
        adOffset = 1
        adInterval = 2
    }

    LazyColumn(
        modifier = modifier.drawVerticalScrollbar(state),
        state = state,
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        items(
            count = pagingAdapter.itemCount,
            key = pagingAdapter.itemKey { it.id.value },
            contentType = pagingAdapter.itemContentType(),
        ) { index ->
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                pagingAdapter[index]?.let { post ->
                    PostItem(
                        modifier = Modifier.fillMaxWidth(),
                        post = post.copy(isBookmarked = bookmarkedPosts.contains(post.id)),
                        isHideAdultContents = userData.isHideAdultContents,
                        isOverrideAdultContents = userData.isAllowedShowAdultContents,
                        isTestUser = userData.isTestUser,
                        onClickPost = { if (!post.isRestricted) onClickPost.invoke(it) },
                        onClickCreator = onClickCreator,
                        onClickPlanList = onClickPlanList,
                        onClickLike = onClickPostLike,
                        onClickBookmark = { _, isBookmarked ->
                            onClickPostBookmark.invoke(post, isBookmarked)
                        },
                    )
                }

                if ((index + adOffset) % adInterval == 0 && !userData.hasPrivilege) {
                    if (currentPlatform == Platform.IOS) {
                        BannerAdView(modifier = Modifier.fillMaxWidth())
                    } else {
                        NativeAdView(modifier = Modifier.fillMaxWidth())
                    }
                }
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

@Composable
private fun GridSection(
    pagingAdapter: LazyPagingItems<FanboxPost>,
    userData: UserData,
    bookmarkedPosts: ImmutableList<PostId>,
    onClickPost: (PostId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyGridState()

    if (pagingAdapter.loadState.append !is LoadState.Error) {
        LazyVerticalGrid(
            modifier = modifier
                .drawVerticalScrollbar(state, spanCount = 2)
                .fillMaxSize(),
            columns = GridCells.Fixed(2),
            state = state,
            verticalArrangement = Arrangement.spacedBy(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            items(
                count = pagingAdapter.itemCount,
                key = pagingAdapter.itemKey { it.id.value },
                contentType = pagingAdapter.itemContentType(),
            ) { index ->
                pagingAdapter[index]?.let { post ->
                    PostGridItem(
                        modifier = Modifier.fillMaxWidth(),
                        post = post.copy(isBookmarked = bookmarkedPosts.contains(post.id)),
                        isHideAdultContents = userData.isHideAdultContents,
                        isOverrideAdultContents = userData.isAllowedShowAdultContents,
                        onClickPost = { if (!post.isRestricted) onClickPost.invoke(it) },
                    )
                }
            }
        }
    } else {
        PagingErrorSection(
            modifier = Modifier.fillMaxSize(),
            onRetry = { pagingAdapter.retry() },
        )
    }
}
