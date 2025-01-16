package me.matsumo.fanbox.feature.post.bookmark

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.bookmark_empty_description
import me.matsumo.fanbox.core.resources.bookmark_empty_title
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.drawVerticalScrollbar
import me.matsumo.fanbox.core.ui.view.ErrorView
import me.matsumo.fanbox.feature.post.bookmark.items.BookmarkedPostsTopBar
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun BookmarkedPostsRoute(
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (FanboxCreatorId) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: BookmarkedPostsViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    LaunchedEffect(true) {
        if (screenState !is ScreenState.Idle) {
            viewModel.fetch()
        }
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        terminate = { terminate.invoke() },
    ) {
        BookmarkedPostsScreen(
            modifier = Modifier.fillMaxSize(),
            userData = it.userData,
            posts = it.posts.toImmutableList(),
            bookmarkedPostIds = it.bookmarkedPostIds.toImmutableList(),
            onSearch = viewModel::search,
            onClickPost = navigateToPostDetail,
            onCLickPostLike = viewModel::postLike,
            onClickPostBookmark = viewModel::postBookmark,
            onClickCreatorPosts = navigateToCreatorPosts,
            onClickCreatorPlans = navigateToCreatorPlans,
            onTerminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BookmarkedPostsScreen(
    userData: UserData,
    posts: ImmutableList<FanboxPost>,
    bookmarkedPostIds: ImmutableList<FanboxPostId>,
    onSearch: (String) -> Unit,
    onClickPost: (FanboxPostId) -> Unit,
    onCLickPostLike: (FanboxPostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreatorPosts: (FanboxCreatorId) -> Unit,
    onClickCreatorPlans: (FanboxCreatorId) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyGridState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val columns = when (LocalNavigationType.current.type) {
        PixiViewNavigationType.BottomNavigation -> 1
        PixiViewNavigationType.NavigationRail -> 2
        PixiViewNavigationType.PermanentNavigationDrawer -> 2
        else -> 1
    }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            BookmarkedPostsTopBar(
                modifier = Modifier.fillMaxWidth(),
                onClickSearch = onSearch,
                onClickTerminate = onTerminate,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            HorizontalDivider()
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            AnimatedContent(
                targetState = posts.isNotEmpty(),
                transitionSpec = { fadeIn().togetherWith(fadeOut()) },
                label = "BookmarkedPostsScreen",
            ) { targetState ->
                if (targetState) {
                    LazyVerticalGrid(
                        modifier = Modifier.drawVerticalScrollbar(state, columns),
                        state = state,
                        columns = GridCells.Fixed(columns),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        items(
                            items = posts,
                            key = { it.id.uniqueValue },
                        ) { likedPost ->
                            PostItem(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth(),
                                post = likedPost,
                                isHideAdultContents = userData.isHideAdultContents,
                                isOverrideAdultContents = userData.isAllowedShowAdultContents,
                                isTestUser = userData.isTestUser,
                                isBookmarked = bookmarkedPostIds.contains(likedPost.id),
                                onClickPost = onClickPost,
                                onClickBookmark = { _, isBookmarked -> onClickPostBookmark.invoke(likedPost, isBookmarked) },
                                onClickCreator = onClickCreatorPosts,
                                onClickLike = onCLickPostLike,
                                onClickPlanList = onClickCreatorPlans,
                            )
                        }

                        item(span = { GridItemSpan(maxLineSpan) }) {
                            Spacer(modifier = Modifier.navigationBarsPadding())
                        }
                    }
                } else {
                    ErrorView(
                        modifier = Modifier.fillMaxSize(),
                        title = Res.string.bookmark_empty_title,
                        message = Res.string.bookmark_empty_description,
                    )
                }
            }
        }
    }
}
