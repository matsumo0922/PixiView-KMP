package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.logs.category.BillingLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.navigation.BottomSheetSceneStrategy

@OptIn(ExperimentalComposeUiApi::class)
fun EntryProviderScope<Destination>.billingPlusBottomSheetEntry(terminate: () -> Unit) {
    entry<Destination.BillingPlusBottomSheet>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) { destination ->
        BackHandler {
            terminate()
        }

        BillingPlusRoute(
            modifier = Modifier.fillMaxSize(),
            initialPlanType = destination.initialPlanType,
            terminate = terminate,
        )
    }
}

/**
 * @param replaceTo この BottomSheet を閉じ、代わりに引数の画面を開く。
 */
@OptIn(ExperimentalComposeUiApi::class)
fun EntryProviderScope<Destination>.billingRetentionBottomSheetEntry(
    replaceTo: (Destination) -> Unit,
    terminate: () -> Unit,
) {
    entry<Destination.BillingRetentionBottomSheet>(
        metadata = BottomSheetSceneStrategy.bottomSheet(
            onUserDismiss = {
                BillingLog.retentionPromptDismissed(BILLING_RETENTION_DISMISS_SHEET).send()
            },
        ),
    ) { destination ->
        BillingRetentionRoute(
            modifier = Modifier.fillMaxSize(),
            isAnnualOfferShown = destination.isAnnualOfferShown,
            replaceTo = replaceTo,
            terminate = terminate,
        )
    }
}

/** scrim タップやスワイプで BottomSheet が閉じたときの dismiss reason。 */
private const val BILLING_RETENTION_DISMISS_SHEET = "sheet_dismiss"
