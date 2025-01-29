package me.matsumo.fanbox.feature.post.search.creator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

const val PostByCreatorCreatorId = "creatorId"
const val PostByCreatorRoute = "postByCreator/{$PostByCreatorCreatorId}"

fun NavController.navigateToPostByCreatorSearch(creatorId: FanboxCreatorId) {
    this.navigateWithLog("postByCreator/$creatorId")
}

fun NavGraphBuilder.postByCreatorSearchScreen(
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (FanboxCreatorId) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.PostByCreatorSearch> {
        PostByCreatorSearchRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToPostDetail = navigateToPostDetail,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            terminate = terminate,
        )
    }
}
