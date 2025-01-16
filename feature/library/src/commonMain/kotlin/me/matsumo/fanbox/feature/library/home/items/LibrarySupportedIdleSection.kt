package me.matsumo.fanbox.feature.library.home.items

import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import app.cash.paging.compose.LazyPagingItems
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

@Composable
internal fun LibrarySupportedIdleSection(
    pagingAdapter: LazyPagingItems<FanboxPost>,
    userData: UserData,
    bookmarkedPostsIds: ImmutableList<FanboxPostId>,
    onClickPost: (FanboxPostId) -> Unit,
    onClickPostLike: (FanboxPostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreator: (FanboxCreatorId) -> Unit,
    onClickPlanList: (FanboxCreatorId) -> Unit,
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

    LibraryHomePagingItems(
        modifier = modifier,
        state = state,
        columns = columns,
        adOffset = adOffset,
        adInterval = adInterval,
        pagingAdapter = pagingAdapter,
        userData = userData,
        bookmarkedPostsIds = bookmarkedPostsIds,
        isGridMode = userData.isUseGridMode,
        onClickPost = onClickPost,
        onClickPostLike = onClickPostLike,
        onClickPostBookmark = onClickPostBookmark,
        onClickCreator = onClickCreator,
        onClickPlanList = onClickPlanList,
    )
}
