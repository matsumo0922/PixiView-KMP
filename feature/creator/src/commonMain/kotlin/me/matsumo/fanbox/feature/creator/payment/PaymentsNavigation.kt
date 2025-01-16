package me.matsumo.fanbox.feature.creator.payment

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavOptions
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

const val PaymentsRoute = "payments"

fun NavController.navigateToPayments(navOptions: NavOptions? = null) {
    this.navigateWithLog(PaymentsRoute, navOptions)
}

fun NavGraphBuilder.paymentsScreen(
    navigateToCreatorPosts: (FanboxCreatorId) -> Unit,
    terminate: () -> Unit,
) {
    composable(PaymentsRoute) {
        PaymentsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToCreatorPosts = navigateToCreatorPosts,
            terminate = terminate,
        )
    }
}
