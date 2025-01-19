@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.feature.post.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

const val PostDetailId = "postDetailId"
const val PostDetailType = "postDetailPagingType"
const val PostDetailRoute = "postDetail/{$PostDetailId}/{$PostDetailType}"

fun NavController.navigateToPostDetail(postId: FanboxPostId, pagingType: PostDetailPagingType) {
    this.navigateWithLog("postDetail/$postId/${pagingType.name}")
}

fun NavGraphBuilder.postDetailScreen(
    navigateToPostSearch: (String, FanboxCreatorId) -> Unit,
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToPostImage: (FanboxPostId, Int) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (FanboxCreatorId) -> Unit,
    navigateToDownloadQueue: () -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    composable(
        route = PostDetailRoute,
        arguments = listOf(
            navArgument(PostDetailId) { type = NavType.StringType },
            navArgument(PostDetailType) {
                type = NavType.StringType
                defaultValue = PostDetailPagingType.Unknown.name
            },
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://{creatorId}.fanbox.cc/posts/{$PostDetailId}" },
        ),
    ) {
        PostDetailRoute(
            modifier = Modifier.fillMaxSize(),
            postId = FanboxPostId(it.arguments?.getString(PostDetailId).orEmpty()),
            type = PostDetailPagingType.valueOf(it.arguments?.getString(PostDetailType) ?: PostDetailPagingType.Unknown.name),
            navigateToPostSearch = navigateToPostSearch,
            navigateToPostDetail = navigateToPostDetail,
            navigateToPostImage = navigateToPostImage,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToDownloadQueue = navigateToDownloadQueue,
            navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
            terminate = terminate,
        )
    }
}

enum class PostDetailPagingType {
    Home,
    Supported,
    Creator,
    Search,
    Unknown,
}
