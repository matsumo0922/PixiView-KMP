@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.feature.post.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.model.fanbox.id.FanboxCreatorId
import me.matsumo.fanbox.core.model.fanbox.id.FanboxPostId
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents

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
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    composable(
        route = PostDetailRoute,
        arguments = listOf(
            navArgument(PostDetailId) { type = NavType.StringType },
            navArgument(PostDetailType) { type = NavType.StringType },
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
