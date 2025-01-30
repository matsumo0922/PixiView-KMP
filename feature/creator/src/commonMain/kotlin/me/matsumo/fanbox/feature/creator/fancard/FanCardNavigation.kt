package me.matsumo.fanbox.feature.creator.fancard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.customNavTypes

fun NavGraphBuilder.fanCardScreen(
    terminate: () -> Unit,
) {
    composable<Destination.FanCard>(
        typeMap = customNavTypes,
    ) {
        FanCardRoute(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
