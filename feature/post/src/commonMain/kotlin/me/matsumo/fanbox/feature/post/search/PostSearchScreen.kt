package me.matsumo.fanbox.feature.post.search

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import app.cash.paging.compose.LazyPagingItems
import app.cash.paging.compose.collectAsLazyPagingItems
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.matsumo.fanbox.core.model.FanboxTag
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.feature.post.search.items.PostSearchCreatorScreen
import me.matsumo.fanbox.feature.post.search.items.PostSearchTagScreen
import me.matsumo.fanbox.feature.post.search.items.PostSearchTopBar
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PostSearchRoute(
    query: String,
    navigateToPostSearch: (CreatorId?, String?, String?) -> Unit,
    navigateToPostDetail: (PostId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToCreatorPlans: (CreatorId) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostSearchViewModel = koinViewModel(),
    navigatorExtension: NavigatorExtension = koinInject(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val creatorPaging = uiState.creatorPaging.collectAsLazyPagingItems()
    val tagPaging = uiState.tagPaging.collectAsLazyPagingItems()

    LaunchedEffect(true) {
        if (query.isNotBlank() && uiState.query.isBlank()) {
            viewModel.search(parseQuery(query))
        }
    }

    PostSearchScreen(
        modifier = modifier,
        query = uiState.query,
        initialQuery = query,
        userData = uiState.userData,
        bookmarkedPosts = uiState.bookmarkedPosts.toImmutableList(),
        suggestTags = uiState.suggestTags.toImmutableList(),
        creatorPaging = creatorPaging,
        tagPaging = tagPaging,
        onTerminate = terminate,
        onClickPost = navigateToPostDetail,
        onClickPostLike = viewModel::postLike,
        onClickPostBookmark = viewModel::postBookmark,
        onClickCreatorPosts = navigateToCreatorPosts,
        onClickCreatorPlans = navigateToCreatorPlans,
        onClickFollow = viewModel::follow,
        onClickUnfollow = viewModel::unfollow,
        onClickSupporting = navigatorExtension::navigateToWebPage,
        onSearch = {
            if (uiState.query.isNotBlank()) {
                navigateToPostSearch.invoke(it.creatorId, it.creatorQuery, it.tag)
            } else {
                viewModel.search(it)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PostSearchScreen(
    query: String,
    initialQuery: String,
    userData: UserData,
    bookmarkedPosts: ImmutableList<PostId>,
    suggestTags: ImmutableList<FanboxTag>,
    creatorPaging: LazyPagingItems<FanboxCreatorDetail>,
    tagPaging: LazyPagingItems<FanboxPost>,
    onSearch: (PostSearchQuery) -> Unit,
    onTerminate: () -> Unit,
    onClickPost: (PostId) -> Unit,
    onClickPostLike: (PostId) -> Unit,
    onClickPostBookmark: (FanboxPost, Boolean) -> Unit,
    onClickCreatorPosts: (CreatorId) -> Unit,
    onClickCreatorPlans: (CreatorId) -> Unit,
    onClickFollow: suspend (String) -> Result<Unit>,
    onClickUnfollow: suspend (String) -> Result<Unit>,
    onClickSupporting: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PostSearchTopBar(
                query = query,
                initialQuery = initialQuery,
                onClickTerminate = onTerminate,
                onClickSearch = onSearch,
                scrollBehavior = scrollBehavior,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        when (parseQuery(query).mode) {
            PostSearchMode.Creator -> {
                PostSearchCreatorScreen(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    pagingAdapter = creatorPaging,
                    suggestTags = suggestTags,
                    onClickCreator = onClickCreatorPosts,
                    onClickFollow = onClickFollow,
                    onClickUnfollow = onClickUnfollow,
                    onClickTag = { onSearch.invoke(parseQuery(it)) },
                    onClickSupporting = onClickSupporting,
                )
            }

            PostSearchMode.Tag -> {
                PostSearchTagScreen(
                    modifier = Modifier
                        .padding(padding)
                        .fillMaxSize(),
                    pagingAdapter = tagPaging,
                    userData = userData,
                    bookmarkedPosts = bookmarkedPosts,
                    onClickPost = onClickPost,
                    onClickPostLike = onClickPostLike,
                    onClickPostBookmark = onClickPostBookmark,
                    onClickCreator = onClickCreatorPosts,
                    onClickPlanList = onClickCreatorPlans,
                )
            }

            else -> {
                // do nothing
            }
        }
    }
}

internal fun buildQuery(
    creatorId: CreatorId?,
    creatorQuery: String?,
    tag: String?,
): String {
    val queryList = mutableListOf<String>()

    if (creatorQuery != null) {
        queryList += creatorQuery
    }

    if (tag != null) {
        queryList += "#$tag"
    }

    if (creatorId != null) {
        queryList += "from:@${creatorId.value}"
    }

    return queryList.joinToString(" ")
}

internal fun buildQuery(query: PostSearchQuery): String {
    return buildQuery(
        creatorId = query.creatorId,
        creatorQuery = query.creatorQuery,
        tag = query.tag,
    )
}

internal fun parseQuery(query: String): PostSearchQuery {
    val queryList = query.split(" ").filter { it.isNotBlank() }

    var mode = PostSearchMode.Unknown
    var creatorId: CreatorId? = null
    var creatorQuery: String? = null
    var tag: String? = null

    queryList.forEach {
        when {
            it.startsWith("from:@") -> {
                mode = PostSearchMode.Tag
                creatorId = CreatorId(it.removePrefix("from:@"))
            }

            it.startsWith("#") -> {
                mode = PostSearchMode.Tag
                tag = it.removePrefix("#")
            }

            else -> {
                mode = PostSearchMode.Creator
                creatorQuery = it
            }
        }
    }

    return PostSearchQuery(
        mode = mode,
        creatorId = creatorId,
        creatorQuery = creatorQuery,
        postQuery = null,
        tag = tag,
    )
}
