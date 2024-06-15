package me.matsumo.fanbox.feature.creator.fancard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val FanCardId = "fanCardId"
const val FanCardRoute = "fanCard/{$FanCardId}"

fun NavController.navigateToFanCard(creatorId: CreatorId) {
    this.navigateWithLog("fanCard/$creatorId")
}

fun NavGraphBuilder.fanCardScreen(
    terminate: () -> Unit,
) {
    composable(
        route = FanCardRoute,
        arguments = listOf(navArgument(FanCardId) { type = NavType.StringType })
    ) {
        FanCardRoute(
            modifier = Modifier.fillMaxSize(),
            creatorId = CreatorId(it.arguments?.getString(FanCardId).orEmpty()),
            terminate = terminate,
        )
    }
}
