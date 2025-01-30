package me.matsumo.fanbox.feature.creator.payment

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination

fun NavGraphBuilder.paymentsScreen(
    navigateTo: (Destination) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.Payments> {
        PaymentsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
