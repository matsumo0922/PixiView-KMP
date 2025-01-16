package me.matsumo.fanbox.feature.post.bookmark

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.fanbox.id.FanboxCreatorId
import me.matsumo.fanbox.core.model.fanbox.id.FanboxPostId
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val BookmarkedPostsRoute = "bookmarkedPosts"

fun NavController.navigateToBookmarkedPosts() {
    this.navigateWithLog(BookmarkedPostsRoute)
}

fun NavGraphBuilder.bookmarkedPostsScreen(
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (FanboxCreatorId) -> Unit,
    terminate: () -> Unit,
) {
    composable(BookmarkedPostsRoute) {
        BookmarkedPostsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToPostDetail = navigateToPostDetail,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            terminate = terminate,
        )
    }
}
