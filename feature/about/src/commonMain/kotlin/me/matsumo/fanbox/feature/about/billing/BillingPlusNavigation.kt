package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.ui.component.sheet.bottomSheet
import me.matsumo.fanbox.core.ui.extensition.BackHandler
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val BillingPlusReferrer = "billingPlusReferrer"
const val BillingPlusRoute = "billingPlus/{$BillingPlusReferrer}"

fun NavController.navigateToBillingPlus(referrerSetting: String? = null) {
    this.navigateWithLog("billingPlus/${referrerSetting.toString()}")
}

fun NavGraphBuilder.billingPlusBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet(
        route = BillingPlusRoute,
        arguments = listOf(
            navArgument(BillingPlusReferrer) { type = NavType.StringType },
        ),
    ) {
        val referrer = it.arguments?.getString(BillingPlusReferrer).toString()

        BackHandler {
            terminate()
        }

        BillingPlusRoute(
            modifier = Modifier.fillMaxSize(),
            referrer = referrer,
            terminate = terminate,
        )
    }
}
