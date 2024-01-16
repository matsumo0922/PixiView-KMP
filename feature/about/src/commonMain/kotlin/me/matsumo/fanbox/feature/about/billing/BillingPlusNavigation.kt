package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.extensition.bottomSheet
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val BillingPlusRoute = "billingPlus"

fun Navigator.navigateToBillingPlus() {
    this.navigate(BillingPlusRoute)
}

fun RouteBuilder.billingPlusBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet(
        route = BillingPlusRoute,
        onDismissRequest = terminate,
        skipPartiallyExpanded = true,
    ) {
        BillingPlusRoute(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
