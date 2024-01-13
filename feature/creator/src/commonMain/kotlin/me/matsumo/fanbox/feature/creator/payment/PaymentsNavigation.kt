package me.matsumo.fanbox.feature.creator.payment

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val PaymentsRoute = "payments"

fun Navigator.navigateToPayments(navOptions: NavOptions? = null) {
    this.navigate(PaymentsRoute, navOptions)
}

fun RouteBuilder.paymentsScreen(
    navigateToCreatorPosts: (CreatorId) -> Unit,
    terminate: () -> Unit,
) {
    scene(
        route = PaymentsRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        PaymentsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToCreatorPosts = navigateToCreatorPosts,
            terminate = terminate,
        )
    }
}
