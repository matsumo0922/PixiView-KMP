package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import me.matsumo.fanbox.core.ui.extensition.bottomSheet

const val BillingPlusRoute = "billingPlus"

fun NavController.navigateToBillingPlus() {
    this.navigate(BillingPlusRoute)
}

fun NavGraphBuilder.billingPlusBottomSheet(
    terminate: () -> Unit,
) {
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
}
