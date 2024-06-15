package me.matsumo.fanbox.feature.library.home.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridState
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
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
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
    val state = rememberLazyGridState()

    val adOffset: Int
    val adInterval: Int

    val columns = if(userData.isUseGridMode) {
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

    LibraryHomePagingItems(
        modifier = modifier,
        state = state,
        columns = columns,
        adOffset = adOffset,
        adInterval = adInterval,
        pagingAdapter = pagingAdapter,
        userData = userData,
        bookmarkedPosts = bookmarkedPosts,
        isGridMode = userData.isUseGridMode,
        onClickPost = onClickPost,
        onClickPostLike = onClickPostLike,
        onClickPostBookmark = onClickPostBookmark,
        onClickCreator = onClickCreator,
        onClickPlanList = onClickPlanList,
    )
}
