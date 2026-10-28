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
import androidx.compose.foundation.lazy.itemsIndexed
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
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.UserId
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
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PostByCreatorSearchRoute(
    navigateTo: (Destination) -> Unit,
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
            setting = uiState.setting,
            creatorDetail = uiState.creatorDetail,
            searchedPosts = uiState.searchedPosts.toImmutableList(),
            bookmarkedPostIds = uiState.bookmarkedPostsIds.toImmutableList(),
            progress = uiState.progress,
            isPrepared = uiState.isPrepared,
            onQueryChange = viewModel::updateQuery,
            onPostClick = { navigateTo(Destination.PostDetail(it, Destination.PostDetail.PagingType.Unknown)) },
            onCreatorPostsClick = { navigateTo(Destination.CreatorTop(it, true)) },
            onCreatorPlansClick = { navigateTo(Destination.CreatorTop(it, false)) },
            onFollowClick = viewModel::follow,
            onUnfollowClick = viewModel::unfollow,
            onSupportingClick = { navigatorExtension.navigateToWebPage(it, "") },
            onLikeClick = viewModel::postLike,
            onBookmarkClick = viewModel::postBookmark,
            onBackClick = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostByCreatorSearchScreen(
    query: String,
    setting: Setting,
    creatorDetail: CreatorDetail,
    searchedPosts: ImmutableList<Post>,
    bookmarkedPostIds: ImmutableList<PostId>,
    progress: Float,
    isPrepared: Boolean,
    onQueryChange: (String) -> Unit,
    onPostClick: (PostId) -> Unit,
    onCreatorPostsClick: (CreatorId) -> Unit,
    onCreatorPlansClick: (CreatorId) -> Unit,
    onFollowClick: suspend (UserId) -> Result<Unit>,
    onUnfollowClick: suspend (UserId) -> Result<Unit>,
    onSupportingClick: (String) -> Unit,
    onLikeClick: (PostId) -> Unit,
    onBookmarkClick: (Post, Boolean) -> Unit,
    onBackClick: () -> Unit,
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
                onClickTerminate = onBackClick,
                onClickSearch = {},
                onQueryChange = onQueryChange,
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
                var isFollowed by rememberSaveable(creatorDetail.isFollowed) {
                    mutableStateOf(creatorDetail.isFollowed)
                }

                CreatorItem(
                    modifier = Modifier.fillMaxWidth(),
                    creatorDetail = creatorDetail,
                    onClickCreator = onCreatorPostsClick,
                    onClickFollow = {
                        scope.launch {
                            isFollowed = true
                            isFollowed = onFollowClick(it).isSuccess
                        }
                    },
                    onClickUnfollow = {
                        scope.launch {
                            isFollowed = false
                            isFollowed = !onUnfollowClick(it).isSuccess
                        }
                    },
                    onClickSupporting = onSupportingClick,
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

            itemsIndexed(
                items = searchedPosts,
                // fankt 0.1.0 で uniqueValue が削除された。ID だけでは同じ投稿が二度現れたときに
                // Key was already used になるため index を併用する。
                key = { index, post -> "${post.id.value}-$index" },
            ) { _, post ->
                PostItem(
                    modifier = Modifier
                        .animateItem()
                        .fillMaxWidth(),
                    post = post,
                    onClickPost = onPostClick,
                    onClickCreator = onCreatorPostsClick,
                    onClickPlanList = onCreatorPlansClick,
                    onClickLike = onLikeClick,
                    onClickBookmark = { _, isBookmarked -> onBookmarkClick.invoke(post, isBookmarked) },
                    isHideAdultContents = setting.isHideAdultContents,
                    isOverrideAdultContents = setting.isOverrideAdultContents,
                    isTestUser = setting.isTestUser,
                    isBookmarked = bookmarkedPostIds.contains(post.id),
                )
            }

            val shouldShowEmptyResult = query.isNotBlank() && searchedPosts.isEmpty() && isPrepared
            if (shouldShowEmptyResult) {
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
