package me.matsumo.fanbox.feature.creator.follow

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val FollowingCreatorsRoute = "followingCreators"

fun Navigator.navigateToFollowingCreators(navOptions: NavOptions? = null) {
    this.navigate(FollowingCreatorsRoute, navOptions)
}

fun RouteBuilder.followingCreatorsScreen(
    navigateToCreatorPlans: (CreatorId) -> Unit,
    terminate: () -> Unit,
) {
    scene(
        route = FollowingCreatorsRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        FollowingCreatorsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToCreatorPlans = navigateToCreatorPlans,
            terminate = terminate,
        )
    }
}
