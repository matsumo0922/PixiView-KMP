package me.matsumo.fanbox.feature.creator.fancard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path

const val FanCardId = "fanCardId"
const val FanCardRoute = "fanCard/{$FanCardId}"

fun Navigator.navigateToFanCard(creatorId: CreatorId) {
    this.navigate("fanCard/$creatorId")
}

fun RouteBuilder.fanCardScreen(
    terminate: () -> Unit,
) {
    scene(
        route = FanCardRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        FanCardRoute(
            modifier = Modifier.fillMaxSize(),
            creatorId = CreatorId(it.path<String>(FanCardId).orEmpty()),
            terminate = terminate,
        )
    }
}
