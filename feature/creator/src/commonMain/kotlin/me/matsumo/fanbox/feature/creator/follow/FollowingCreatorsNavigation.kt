package me.matsumo.fanbox.feature.creator.follow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable

const val FollowingCreatorsRoute = "followingCreators"

fun NavController.navigateToFollowingCreators(navOptions: NavOptions? = null) {
    this.navigate(FollowingCreatorsRoute, navOptions)
}

fun NavGraphBuilder.followingCreatorsScreen(
    navigateToCreatorPlans: (CreatorId) -> Unit,
    terminate: () -> Unit,
) {
    composable(FollowingCreatorsRoute) {
        FollowingCreatorsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToCreatorPlans = navigateToCreatorPlans,
            terminate = terminate,
        )
    }
}
