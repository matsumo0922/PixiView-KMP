package me.matsumo.fanbox.feature.about.versions

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.bottomSheet
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.SwipeProperties
import moe.tlaster.precompose.navigation.transition.NavTransition

const val VersionHistoryRoute = "versionHistory"

fun Navigator.navigateToVersionHistory() {
    this.navigate("versionHistory")
}

fun RouteBuilder.versionHistoryBottomSheet(
    terminate: () -> Unit,
) {
    if (currentPlatform == Platform.Android) {
        bottomSheet(
            route = VersionHistoryRoute,
            skipPartiallyExpanded = true,
            onDismissRequest = terminate,
        ) {
            VersionHistoryDialog(
                modifier = Modifier.fillMaxSize(),
                terminate = terminate,
            )
        }
    } else {
        scene(
            route = VersionHistoryRoute,
            swipeProperties = SwipeProperties(isEnable = false),
            navTransition = NavTransition(
                createTransition = slideInVertically { it },
                destroyTransition = slideOutVertically { it },
                pauseTransition = slideOutVertically { -it / 4 },
                resumeTransition = slideInVertically { -it / 4 },
                exitTargetContentZIndex = 1f,
            )
        ) {
            VersionHistoryDialog(
                modifier = Modifier.fillMaxSize(),
                terminate = terminate,
            )
        }
    }
}
