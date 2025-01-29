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
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

const val PostDetailId = "postDetailId"


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
    composable<Destination.PostDetail>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://www.fanbox.cc/@{creatorId}/posts/{$PostDetailId}" },
            navDeepLink { uriPattern = "https://{creatorId}.fanbox.cc/posts/{$PostDetailId}" },
        ),
    ) {
        PostDetailRoute(
            modifier = Modifier.fillMaxSize(),
            postId = it.toRoute<Destination.PostDetail>().postId,
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
