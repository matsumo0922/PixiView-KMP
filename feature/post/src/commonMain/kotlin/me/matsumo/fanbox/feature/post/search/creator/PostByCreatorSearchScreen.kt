package me.matsumo.fanbox.feature.post.search.creator

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.creator_posts_download_dialog_title
import me.matsumo.fanbox.core.resources.error_no_data
import me.matsumo.fanbox.core.resources.error_no_data_search
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.component.CreatorItem
import me.matsumo.fanbox.core.ui.component.PostItem
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.plus
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.view.ErrorView
import me.matsumo.fanbox.feature.post.search.common.items.PostSearchTopBar
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
internal fun PostByCreatorSearchRoute(
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (FanboxCreatorId) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    navigatorExtension: NavigatorExtension = koinInject(),
    viewModel: PostByCreatorSearchViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val query by viewModel.query.collectAsStateWithLifecycle()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { viewModel.fetch() },
    ) { uiState ->
        PostByCreatorSearchScreen(
            modifier = Modifier.fillMaxSize(),
            query = query,
            userData = uiState.userData,
            creatorDetail = uiState.creatorDetail,
            searchedPosts = uiState.searchedPosts.toImmutableList(),
            bookmarkedPostIds = uiState.bookmarkedPostsIds.toImmutableList(),
            progress = uiState.progress,
            isPrepared = uiState.isPrepared,
            onQueryChanged = viewModel::updateQuery,
            onPostClicked = navigateToPostDetail,
            onCreatorPostsClicked = navigateToCreatorPosts,
            onCreatorPlansClicked = navigateToCreatorPlans,
            onFollowClicked = viewModel::follow,
            onUnfollowClicked = viewModel::unfollow,
            onSupportingClicked = { navigatorExtension.navigateToWebPage(it, PostByCreatorRoute) },
            onLikeClicked = viewModel::postLike,
            onBookmarkClicked = viewModel::postBookmark,
            onBackClicked = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostByCreatorSearchScreen(
    query: String,
    userData: UserData,
    creatorDetail: FanboxCreatorDetail,
    searchedPosts: ImmutableList<FanboxPost>,
    bookmarkedPostIds: ImmutableList<FanboxPostId>,
    progress: Float,
    isPrepared: Boolean,
    onQueryChanged: (String) -> Unit,
    onPostClicked: (FanboxPostId) -> Unit,
    onCreatorPostsClicked: (FanboxCreatorId) -> Unit,
    onCreatorPlansClicked: (FanboxCreatorId) -> Unit,
    onFollowClicked: suspend (FanboxUserId) -> Result<Unit>,
    onUnfollowClicked: suspend (FanboxUserId) -> Result<Unit>,
    onSupportingClicked: (String) -> Unit,
    onLikeClicked: (FanboxPostId) -> Unit,
    onBookmarkClicked: (FanboxPost, Boolean) -> Unit,
    onBackClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PostSearchTopBar(
                query = "",
                initialQuery = query,
                onClickTerminate = onBackClicked,
                onClickSearch = {},
                onQueryChanged = onQueryChanged,
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = padding + PaddingValues(16.dp),
        ) {
            item("creator") {
                var isFollowed by rememberSaveable(creatorDetail.isFollowed) { mutableStateOf(creatorDetail.isFollowed) }

                CreatorItem(
                    modifier = Modifier.fillMaxWidth(),
                    creatorDetail = creatorDetail,
                    onClickCreator = onCreatorPostsClicked,
                    onClickFollow = {
                        scope.launch {
                            isFollowed = true
                            isFollowed = onFollowClicked(it).isSuccess
                        }
                    },
                    onClickUnfollow = {
                        scope.launch {
                            isFollowed = false
                            isFollowed = !onUnfollowClicked(it).isSuccess
                        }
                    },
                    onClickSupporting = onSupportingClicked,
                    isFollowed = isFollowed,
                    isShowCoverImage = false,
                    isShowDescription = false,
                )
            }

            if (query.isNotBlank() && !isPrepared) {
                item("loading") {
                    LoadingItem(
                        modifier = Modifier.fillMaxWidth(),
                        progress = progress,
                    )
                }
            }

            items(
                items = searchedPosts,
                key = { it.id.uniqueValue },
            ) {
                PostItem(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
                    post = it,
                    onClickPost = onPostClicked,
                    onClickCreator = onCreatorPostsClicked,
                    onClickPlanList = onCreatorPlansClicked,
                    onClickLike = onLikeClicked,
                    onClickBookmark = { _, isBookmarked -> onBookmarkClicked.invoke(it, isBookmarked) },
                    isHideAdultContents = userData.isHideAdultContents,
                    isOverrideAdultContents = userData.isOverrideAdultContents,
                    isTestUser = userData.isTestUser,
                    isBookmarked = bookmarkedPostIds.contains(it.id),
                )
            }

            if (query.isNotBlank() && searchedPosts.isEmpty() && isPrepared) {
                item("empty") {
                    ErrorView(
                        modifier = Modifier.fillMaxWidth(),
                        title = Res.string.error_no_data,
                        message = Res.string.error_no_data_search,
                        serviceStatus = false,
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.navigationBarsPadding())
            }
        }
    }
}

@Composable
private fun LoadingItem(
    progress: Float,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 8.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Text(
            text = stringResource(Res.string.creator_posts_download_dialog_title),
            style = MaterialTheme.typography.titleMedium.bold(),
            color = MaterialTheme.colorScheme.onSurface,
        )

        AnimatedContent(
            targetState = progress,
            transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        ) {
            Text(
                text = "%.2f %%".format(it * 100),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
