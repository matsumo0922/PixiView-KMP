package me.matsumo.fanbox.feature.creator.top.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.itemKey
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorTag
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.ads.NativeAdView
import me.matsumo.fanbox.core.ui.component.PostGridItem
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.view.PagingErrorSection

@Composable
internal fun CreatorTopPostsScreen(
    userData: UserData,
    bookmarkedPosts: ImmutableList<PostId>,
    pagingAdapter: LazyPagingItems<FanboxPost>,
    creatorTags: ImmutableList<FanboxCreatorTag>,
    onClickPost: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickTag: (FanboxCreatorTag) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPlanList: (CreatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyGridState()

    val adOffset: Int
    val adInterval: Int

    val columns = if (userData.isUseGridMode) {
        when (LocalNavigationType.current.type) {
            PixiViewNavigationType.BottomNavigation -> 2
            PixiViewNavigationType.NavigationRail -> 3
            PixiViewNavigationType.PermanentNavigationDrawer -> 4
            else -> 2
        }
    } else {
        when (LocalNavigationType.current.type) {
            PixiViewNavigationType.BottomNavigation -> 1
            PixiViewNavigationType.NavigationRail -> 2
            PixiViewNavigationType.PermanentNavigationDrawer -> 2
            else -> 1
        }
    }

    if (currentPlatform == Platform.Android) {
        adOffset = 3
        adInterval = 4
    } else {
        adOffset = 1
        adInterval = 3
    }

    PagingItems(
        modifier = modifier,
        state = state,
        columns = columns,
        adOffset = adOffset,
        adInterval = adInterval,
        pagingAdapter = pagingAdapter,
        userData = userData,
        creatorTags = creatorTags,
        bookmarkedPosts = bookmarkedPosts,
        isGridMode = userData.isUseGridMode,
        onClickPost = onClickPost,
        onClickPostLike = onClickPostLike,
        onClickPostBookmark = onClickPostBookmark,
        onClickCreator = onClickCreator,
        onClickPlanList = onClickPlanList,
        onClickTag = onClickTag,
    )
}

@Composable
private fun PagingItems(
    state: LazyGridState,
    columns: Int,
    adOffset: Int,
    adInterval: Int,
    userData: UserData,
    bookmarkedPosts: ImmutableList<PostId>,
    pagingAdapter: LazyPagingItems<FanboxPost>,
    creatorTags: ImmutableList<FanboxCreatorTag>,
    isGridMode: Boolean,
    onClickPost: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickTag: (FanboxCreatorTag) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPlanList: (CreatorId) -> Unit,
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
        if (creatorTags.isNotEmpty()) {
            item(span = { GridItemSpan(maxLineSpan) }) {
                LazyRow(
                    modifier = Modifier
                        .height(80.dp + if (!isGridMode) 0.dp else 32.dp)
                        .fillMaxWidth(),
                    contentPadding = PaddingValues(if (!isGridMode) 0.dp else 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(creatorTags) {
                        TagItem(
                            tag = it,
                            onClickTag = onClickTag,
                        )
                    }
                }
            }
        }

        items(
            count = pagingAdapter.itemCount + if (userData.hasPrivilege) 0 else (pagingAdapter.itemCount / adInterval),
            key = { index ->
                when {
                    userData.hasPrivilege -> pagingAdapter.itemKey { it.id.uniqueValue }(index)
                    (index + adOffset) % adInterval == 0 -> "ad-$index"
                    else -> pagingAdapter.itemKey { it.id.uniqueValue }(index - ((index + adOffset) / adInterval))
                }
            },
        ) { index ->
            if ((index + adOffset) % adInterval == 0 && !userData.hasPrivilege) {
                NativeAdView(
                    modifier = Modifier.fillMaxSize(),
                    key = "$index",
                )
            } else {
                pagingAdapter[if (userData.hasPrivilege) index else index - ((index + adOffset) / adInterval)]?.let { post ->
                    if (isGridMode) {
                        PostGridItem(
                            modifier = Modifier.fillMaxWidth(),
                            post = post.copy(isBookmarked = bookmarkedPosts.contains(post.id)),
                            isHideAdultContents = userData.isHideAdultContents,
                            isOverrideAdultContents = userData.isAllowedShowAdultContents,
                            onClickPost = { if (!post.isRestricted) onClickPost.invoke(it) },
                        )
                    } else {
                        PostItem(
                            modifier = Modifier.fillMaxSize(),
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

@Composable
private fun TagItem(
    tag: FanboxCreatorTag,
    onClickTag: (FanboxCreatorTag) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .aspectRatio(5 / 2f, true)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClickTag.invoke(tag) },
    ) {
        if (tag.coverImageUrl != null) {
            SubcomposeAsyncImage(
                modifier = Modifier.fillMaxSize(),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .fanboxHeader()
                    .data(tag.coverImageUrl)
                    .build(),
                loading = {
                    FadePlaceHolder()
                },
                contentScale = ContentScale.Crop,
                contentDescription = null,
            )
        } else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.secondaryContainer),
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
        )

        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterHorizontally,
            ),
        ) {
            Text(
                modifier = Modifier.weight(1f, fill = false),
                text = "#${tag.name}",
                style = MaterialTheme.typography.bodyLarge.bold().copy(
                    shadow = Shadow(
                        color = Color.Black,
                        offset = Offset(1f, 1f),
                        blurRadius = 3f,
                    ),
                ),
                color = MaterialTheme.colorScheme.onSurface,
                overflow = TextOverflow.Ellipsis,
            )

            Card(
                shape = CircleShape,
                colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
            ) {
                Text(
                    modifier = Modifier.padding(8.dp, 6.dp),
                    text = tag.count.toString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
