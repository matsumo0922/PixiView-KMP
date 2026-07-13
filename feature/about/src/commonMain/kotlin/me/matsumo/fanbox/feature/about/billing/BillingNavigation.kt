package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.logs.category.BillingLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.component.sheet.bottomSheet
import me.matsumo.fanbox.core.ui.customNavTypes

@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.billingPlusBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet<Destination.BillingPlusBottomSheet>(
        typeMap = customNavTypes,
    ) { entry ->
        val args = entry.toRoute<Destination.BillingPlusBottomSheet>()

        BackHandler {
            terminate()
        }

        BillingPlusRoute(
            modifier = Modifier.fillMaxSize(),
            initialPlanType = args.initialPlanType,
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.billingRetentionBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet<Destination.BillingRetentionBottomSheet>(
        onDismissed = {
            BillingLog.retentionPromptDismissed(BILLING_RETENTION_DISMISS_SHEET).send()
        },
    ) { entry ->
        val args = entry.toRoute<Destination.BillingRetentionBottomSheet>()

        BillingRetentionRoute(
            modifier = Modifier.fillMaxSize(),
            isAnnualOfferShown = args.isAnnualOfferShown,
            terminate = terminate,
        )
    }
}

/** scrim タップやスワイプで BottomSheet が閉じたときの dismiss reason。 */
private const val BILLING_RETENTION_DISMISS_SHEET = "sheet_dismiss"
