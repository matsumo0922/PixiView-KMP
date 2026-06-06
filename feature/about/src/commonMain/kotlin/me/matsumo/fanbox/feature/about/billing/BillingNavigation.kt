package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.NavGraphBuilder
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.component.sheet.bottomSheet

@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.billingPlusBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet<Destination.BillingPlusBottomSheet> {
        BackHandler {
            terminate()
        }

        BillingPlusRoute(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
