package me.matsumo.fanbox.feature.library.home.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Modifier
import app.cash.paging.compose.LazyPagingItems
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.model.OfferBanner
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform

@Composable
internal fun LibraryHomeIdleSection(
    pagingAdapter: LazyPagingItems<Post>,
    setting: Setting,
    bookmarkedPostsIds: ImmutableList<PostId>,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (Post, Boolean) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickPlanList: (CreatorId) -> Unit,
    offerBanner: OfferBanner?,
    onOfferBannerShow: (OfferBanner) -> Unit,
    onOfferBannerClick: (OfferBanner) -> Unit,
    onOfferBannerDismiss: (OfferBanner) -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyGridState()

    val adOffset: Int
    val adInterval: Int

    val columns = if (setting.isUseGridMode) {
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

    ScrollToInsertedHeaderEffect(
        state = state,
        hasHeader = offerBanner != null,
    )

    if (offerBanner != null) {
        OfferBannerShowEffect(
            state = state,
            banner = offerBanner,
            onShow = onOfferBannerShow,
        )
    }

    LibraryHomePagingItems(
        modifier = modifier,
        state = state,
        columns = columns,
        adOffset = adOffset,
        adInterval = adInterval,
        pagingAdapter = pagingAdapter,
        setting = setting,
        bookmarkedPostsIds = bookmarkedPostsIds,
        isGridMode = setting.isUseGridMode,
        onClickPost = onClickPost,
        onClickPostLike = onClickPostLike,
        onClickPostBookmark = onClickPostBookmark,
        onClickCreator = onClickCreator,
        onClickPlanList = onClickPlanList,
        header = offerBanner?.let { banner ->
            {
                LibraryHomeOfferBanner(
                    modifier = Modifier.fillMaxWidth(),
                    banner = banner,
                    onClick = { onOfferBannerClick(banner) },
                    onDismiss = { onOfferBannerDismiss(banner) },
                )
            }
        },
    )
}

/**
 * 一覧の先頭を表示している間に header が後から挿入された場合に、先頭（header）までスクロールする。
 *
 * 一覧は表示中の投稿の位置を保つため、そのままでは header が画面の上の外に置かれる。最初の composition の時点で header があった場合
 * （画面の復元など）は挿入とみなさず、スクロールしない。
 */
@Composable
private fun ScrollToInsertedHeaderEffect(
    state: LazyGridState,
    hasHeader: Boolean,
) {
    var hadHeader by remember { mutableStateOf(hasHeader) }

    LaunchedEffect(hasHeader) {
        val isInserted = hasHeader && !hadHeader
        hadHeader = hasHeader
        if (isInserted && state.isAtTopBeforeHeaderInsertion()) state.scrollToItem(0)
    }
}

/** header の挿入が一覧の位置に反映される前（index 0）と後（index 1）のどちらでも、先頭を表示しているかを返す。 */
private fun LazyGridState.isAtTopBeforeHeaderInsertion(): Boolean = firstVisibleItemIndex <= 1 && firstVisibleItemScrollOffset == 0

/** オファーのバナーが画面に入ったときに、表示を知らせる。残り時間の更新では送り直さないよう、オファーの種類と周期が変わったときだけ知らせる。 */
@Composable
private fun OfferBannerShowEffect(
    state: LazyGridState,
    banner: OfferBanner,
    onShow: (OfferBanner) -> Unit,
) {
    val currentBanner by rememberUpdatedState(banner)
    val currentOnShow by rememberUpdatedState(onShow)

    LaunchedEffect(state, banner.logValue, banner.cycleNumber) {
        snapshotFlow { state.layoutInfo.visibleItemsInfo.any { item -> item.key == LIBRARY_HOME_HEADER_KEY } }.first { isVisible -> isVisible }
        currentOnShow(currentBanner)
    }
}
