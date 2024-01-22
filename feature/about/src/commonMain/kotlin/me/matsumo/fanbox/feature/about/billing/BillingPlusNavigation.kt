package me.matsumo.fanbox.feature.about.billing

import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.transition.NavTransition

const val BillingPlusRoute = "billingPlus"

fun Navigator.navigateToBillingPlus() {
    this.navigate(BillingPlusRoute)
}

fun RouteBuilder.billingPlusBottomSheet(
    terminate: () -> Unit,
) {
    scene(
        route = BillingPlusRoute,
        navTransition = NavTransition(
            createTransition = slideInVertically { it },
            destroyTransition = slideOutVertically { it },
            pauseTransition = slideOutVertically { -it / 4 } + scaleOut(targetScale = 0.9f),
            resumeTransition = slideInVertically { -it / 4 } + scaleIn(initialScale = 0.9f),
            exitTargetContentZIndex = 1f
        )
    ) {
        BillingPlusRoute(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
