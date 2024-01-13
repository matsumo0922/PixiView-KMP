package me.matsumo.fanbox.feature.creator.support

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val SupportingCreatorsRoute = "supportingCreators"

fun Navigator.navigateToSupportingCreators(navOptions: NavOptions? = null) {
    this.navigate(SupportingCreatorsRoute, navOptions)
}

fun RouteBuilder.supportingCreatorsScreen(
    navigateToCreatorPlans: (CreatorId) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToFanCard: (CreatorId) -> Unit,
    terminate: () -> Unit,
) {
    scene(
        route = SupportingCreatorsRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        SupportingCreatorsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToFanCard = navigateToFanCard,
            terminate = terminate,
        )
    }
}
