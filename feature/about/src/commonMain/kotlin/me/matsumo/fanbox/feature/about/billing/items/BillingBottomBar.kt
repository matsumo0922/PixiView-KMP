package me.matsumo.fanbox.feature.about.billing.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_plan_monthly
import me.matsumo.fanbox.core.resources.billing_plus_plan_yearly
import me.matsumo.fanbox.core.resources.billing_plus_purchase_button
import me.matsumo.fanbox.core.resources.billing_plus_verify_button
import me.matsumo.fanbox.core.resources.error_unknown
import me.matsumo.fanbox.core.resources.unit_month
import me.matsumo.fanbox.core.resources.unit_year
import me.matsumo.fanbox.feature.about.billing.BillingUiState
import me.matsumo.fanbox.feature.about.billing.BillingViewEvent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BillingBottomBar(
    uiState: BillingUiState,
    onViewEvent: (BillingViewEvent) -> Unit,
    modifier: Modifier = Modifier,
) {
    val selectedPlan = remember(uiState.selectedPlanType) {
        uiState.plans.find { it.type == uiState.selectedPlanType }
    }

    Column(
        modifier = modifier
            .background(
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            )
            .padding(top = 16.dp)
            .padding(horizontal = 16.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        PlanSelectSection(
            modifier = Modifier.fillMaxWidth(),
            plans = uiState.plans,
            selectedPlanType = uiState.selectedPlanType,
            onPlanSelected = { onViewEvent(BillingViewEvent.OnPlanSelected(it)) },
        )

        if (selectedPlan != null) {
            ButtonSection(
                modifier = Modifier.fillMaxWidth(),
                selectedPlan = selectedPlan,
                onPurchaseClicked = { onViewEvent(BillingViewEvent.OnPurchaseClicked) },
                onRestoreClicked = { onViewEvent(BillingViewEvent.OnRestoreClicked) },
            )
        }
    }
}

@Composable
private fun PlanSelectSection(
    plans: ImmutableList<BillingPlan>,
    selectedPlanType: BillingPlan.Type,
    onPlanSelected: (BillingPlan.Type) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        plans.forEach {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (selectedPlanType == it.type),
                        onClick = { onPlanSelected.invoke(it.type) },
                        role = Role.RadioButton,
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .border(
                        width = 1.dp,
                        color = if (selectedPlanType == it.type) MaterialTheme.colorScheme.primary else Color.Transparent,
                        shape = RoundedCornerShape(8.dp),
                    )
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                RadioButton(
                    selected = (selectedPlanType == it.type),
                    onClick = null,
                )

                Text(
                    text = when (it.type) {
                        BillingPlan.Type.MONTHLY -> stringResource(Res.string.billing_plus_plan_monthly)
                        BillingPlan.Type.ANNUAL -> stringResource(Res.string.billing_plus_plan_yearly)
                        BillingPlan.Type.UNKNOWN -> stringResource(Res.string.error_unknown)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )

                Text(
                    text = "${it.formattedPrice} / " + when (it.type) {
                        BillingPlan.Type.MONTHLY -> stringResource(Res.string.unit_month)
                        BillingPlan.Type.ANNUAL -> stringResource(Res.string.unit_year)
                        BillingPlan.Type.UNKNOWN -> stringResource(Res.string.error_unknown)
                    },
                    style = MaterialTheme.typography.titleMedium,
                )
            }
        }
    }
}

@Composable
private fun ButtonSection(
    selectedPlan: BillingPlan,
    onPurchaseClicked: () -> Unit,
    onRestoreClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            onClick = onPurchaseClicked,
        ) {
            Text(
                text = stringResource(Res.string.billing_plus_purchase_button, selectedPlan.formattedPrice, selectedPlan.getUnit()),
            )
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onRestoreClicked,
        ) {
            Text(
                text = stringResource(Res.string.billing_plus_verify_button),
            )
        }
    }
}

@Composable
private fun BillingPlan.getUnit() = when (type) {
    BillingPlan.Type.MONTHLY -> Res.string.unit_month
    BillingPlan.Type.ANNUAL -> Res.string.unit_year
    BillingPlan.Type.UNKNOWN -> Res.string.error_unknown
}.let {
    stringResource(it)
}
