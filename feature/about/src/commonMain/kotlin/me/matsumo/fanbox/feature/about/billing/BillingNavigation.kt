package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.NavGraphBuilder
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.logs.category.BillingLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.component.sheet.bottomSheet

@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.billingPlusBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet<Destination.BillingPlusBottomSheet> { entry ->
        val args = entry.toRoute<Destination.BillingPlusBottomSheet>()

        BackHandler {
            terminate()
        }

        BillingPlusRoute(
            modifier = Modifier.fillMaxSize(),
            initialPlanType = args.initialPlanTypeName.toBillingPlanType(),
        )
    }
}

@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.billingRetentionBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet<Destination.BillingRetentionBottomSheet> { entry ->
        val args = entry.toRoute<Destination.BillingRetentionBottomSheet>()

        BackHandler {
            BillingLog.retentionPromptDismissed("back").send()
            terminate()
        }

        BillingRetentionRoute(
            modifier = Modifier.fillMaxSize(),
            isAnnualOfferShown = args.isAnnualOfferShown,
            terminate = terminate,
        )
    }
}

private fun String?.toBillingPlanType(): BillingPlan.Type {
    return BillingPlan.Type.entries.firstOrNull { planType -> planType.name == this } ?: BillingPlan.Type.MONTHLY
}
