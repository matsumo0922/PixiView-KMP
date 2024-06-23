package me.matsumo.fanbox.feature.about.billing.items

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.billing_plus_plan_monthly
import me.matsumo.fanbox.core.ui.billing_plus_plan_yearly
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.unit_month
import me.matsumo.fanbox.core.ui.unit_year
import me.matsumo.fanbox.feature.about.billing.BillingPlusUiState
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun BillingPlusPlanItem(
    planType: BillingPlusUiState.Type,
    formattedPrice: String,
    formattedAnnualMonthlyPrice: String,
    formattedAnnualDiscountRate: String,
    onClick: () -> Unit,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
) {
    val planName: String
    val price: String
    val description: String?

    when (planType) {
        BillingPlusUiState.Type.MONTHLY -> {
            planName = stringResource(Res.string.billing_plus_plan_monthly)
            price = "$formattedPrice / ${stringResource(Res.string.unit_month)}"
            description = null
        }
        BillingPlusUiState.Type.YEARLY -> {
            planName = stringResource(Res.string.billing_plus_plan_yearly)
            price = "$formattedPrice / ${stringResource(Res.string.unit_year)}"
            description = "($formattedAnnualMonthlyPrice / ${stringResource(Res.string.unit_month)})"
        }
    }

    val borderColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.primary else Color.Transparent,
        animationSpec = tween(300),
        label = "borderColor",
    )

    val containerColor by animateColorAsState(
        targetValue = if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent,
        animationSpec = tween(300),
        label = "containerColor",
    )

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color =  borderColor,
                shape = RoundedCornerShape(8.dp),
            )
            .clickable { onClick.invoke() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp, 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            RadioButton(
                selected = isSelected,
                onClick = onClick,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        text = planName,
                        style = MaterialTheme.typography.bodyLarge.bold(),
                        fontSize = 18.sp,
                    )

                    Text(
                        text = price,
                        style = MaterialTheme.typography.bodyLarge,
                    )
                }

                description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            if (planType == BillingPlusUiState.Type.YEARLY) {
                Text(
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary)
                        .padding(12.dp, 6.dp),
                    text = "$formattedAnnualDiscountRate OFF",
                    style = MaterialTheme.typography.bodyMedium.bold(),
                    color = MaterialTheme.colorScheme.onPrimary,
                )
            }
        }
    }
}