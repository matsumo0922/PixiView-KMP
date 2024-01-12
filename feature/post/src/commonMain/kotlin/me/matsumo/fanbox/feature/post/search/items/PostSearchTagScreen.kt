package me.matsumo.fanbox.feature.post.search.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.LazyPagingItemsLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.view.PagingErrorSection

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun PostSearchTagScreen(
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
    val keyboardController = LocalSoftwareKeyboardController.current
    val state = rememberLazyListState()

    LaunchedEffect(state) {
        snapshotFlow { state.layoutInfo }.collect {
            keyboardController?.hide()
        }
    }

    LazyPagingItemsLoadContents(
        modifier = modifier,
        lazyPagingItems = pagingAdapter,
        emptyMessageRes = MR.strings.error_no_data_search,
    ) {
        LazyColumn(
            modifier = Modifier.drawVerticalScrollbar(state),
            state = state,
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(
                count = pagingAdapter.itemCount,
                key = pagingAdapter.itemKey { it.id.value },
                contentType = pagingAdapter.itemContentType(),
            ) { index ->
                pagingAdapter[index]?.let { post ->
                    PostItem(
                        modifier = Modifier.fillMaxWidth(),
                        post = post.copy(isBookmarked = bookmarkedPosts.contains(post.id)),
                        isHideAdultContents = userData.isHideAdultContents,
                        isOverrideAdultContents = userData.isAllowedShowAdultContents,
                        isTestUser = userData.isTestUser,
                        onClickPost = { if (!post.isRestricted) onClickPost.invoke(it) },
                        onClickLike = onClickPostLike,
                        onClickBookmark = { _, isLiked -> onClickPostBookmark.invoke(post, isLiked) },
                        onClickCreator = onClickCreator,
                        onClickPlanList = onClickPlanList,
                    )
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
