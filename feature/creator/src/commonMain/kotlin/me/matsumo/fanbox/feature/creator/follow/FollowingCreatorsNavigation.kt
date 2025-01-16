package me.matsumo.fanbox.feature.creator.follow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

const val FollowingCreatorsRoute = "followingCreators"

fun NavController.navigateToFollowingCreators(navOptions: NavOptions? = null) {
    this.navigateWithLog(FollowingCreatorsRoute, navOptions)
}

fun NavGraphBuilder.followingCreatorsScreen(
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    terminate: () -> Unit,
) {
    composable(FollowingCreatorsRoute) {
        FollowingCreatorsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToCreatorPosts = navigateToCreatorPosts,
            terminate = terminate,
        )
    }
}
