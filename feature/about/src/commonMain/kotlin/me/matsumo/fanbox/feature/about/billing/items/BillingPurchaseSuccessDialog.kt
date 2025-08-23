package me.matsumo.fanbox.feature.about.billing.items

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_purchase_message
import me.matsumo.fanbox.core.resources.billing_plus_purchase_title
import me.matsumo.fanbox.core.resources.common_ok
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BillingPurchaseSuccessDialog(
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
) {
    AlertDialog(
        modifier = modifier,
        onDismissRequest = onDismissRequest,
        title = {
            Text(stringResource(Res.string.billing_plus_purchase_title))
        },
        text = {
            Text(stringResource(Res.string.billing_plus_purchase_message))
        },
        confirmButton = {
            TextButton(onDismissRequest) {
                Text(stringResource(Res.string.common_ok))
            }
        }
    )
}
