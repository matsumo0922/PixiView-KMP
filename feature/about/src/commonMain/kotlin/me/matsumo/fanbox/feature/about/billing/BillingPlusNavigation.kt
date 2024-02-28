package me.matsumo.fanbox.feature.about.billing

import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.bottomSheet
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.transition.NavTransition

const val BillingPlusRoute = "billingPlus"

fun Navigator.navigateToBillingPlus() {
    this.navigate(BillingPlusRoute)
}

fun RouteBuilder.billingPlusBottomSheet(
    terminate: () -> Unit,
) {
    if (currentPlatform == Platform.Android) {
        bottomSheet(
            route = BillingPlusRoute,
            skipPartiallyExpanded = true,
            onDismissRequest = terminate,
        ) {
            BillingPlusRoute(
                modifier = Modifier.fillMaxSize(),
                terminate = terminate,
            )
        }
    } else {
        scene(
            route = BillingPlusRoute,
            swipeProperties = SwipeProperties(isEnable = false),
            navTransition = NavTransition(
                createTransition = slideInVertically { it },
                destroyTransition = slideOutVertically { it },
                pauseTransition = slideOutVertically { -it / 4 },
                resumeTransition = slideInVertically { -it / 4 },
                exitTargetContentZIndex = 1f,
            )
        ) {
            BillingPlusRoute(
                modifier = Modifier.fillMaxSize(),
                terminate = terminate,
            )
        }
    }
}
