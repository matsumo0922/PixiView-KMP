package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.VolunteerActivism
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.logs.category.BillingLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Platform
import me.matsumo.fanbox.core.model.currentPlatform
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_retention_annual_description
import me.matsumo.fanbox.core.resources.billing_retention_annual_title
import me.matsumo.fanbox.core.resources.billing_retention_benefit_bulk_download
import me.matsumo.fanbox.core.resources.billing_retention_benefit_grid
import me.matsumo.fanbox.core.resources.billing_retention_benefit_hide_ads
import me.matsumo.fanbox.core.resources.billing_retention_benefit_support
import me.matsumo.fanbox.core.resources.billing_retention_benefits_title
import me.matsumo.fanbox.core.resources.billing_retention_manage_button
import me.matsumo.fanbox.core.resources.billing_retention_message
import me.matsumo.fanbox.core.resources.billing_retention_pause_description
import me.matsumo.fanbox.core.resources.billing_retention_pause_title
import me.matsumo.fanbox.core.resources.billing_retention_primary_button
import me.matsumo.fanbox.core.resources.billing_retention_title
import me.matsumo.fanbox.core.resources.common_close
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.theme.LocalNavController
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Composable
internal fun BillingRetentionRoute(
    isAnnualOfferShown: Boolean,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    navigatorExtension: NavigatorExtension = koinInject(),
) {
    val navController = LocalNavController.current

    BillingRetentionScreen(
        modifier = modifier,
        isAnnualOfferShown = isAnnualOfferShown,
        onAnnualPlanClicked = {
            BillingLog.retentionPromptAnnualClicked().send()
            navController.navigate(
                Destination.BillingPlusBottomSheet(
                    referrer = BILLING_RETENTION_REFERRER,
                    initialPlanTypeName = BillingPlan.Type.ANNUAL.name,
                ),
            )
        },
        onManageSubscriptionClicked = {
            val subscriptionManagementUrl = currentPlatform.subscriptionManagementUrl()

            BillingLog.retentionPromptManageClicked(currentPlatform.name).send()
            navigatorExtension.navigateToWebPage(
                url = subscriptionManagementUrl,
                referrer = BILLING_RETENTION_REFERRER,
            )
        },
        onDismissClicked = {
            BillingLog.retentionPromptDismissed(BILLING_RETENTION_DISMISS_CLOSE_BUTTON).send()
            terminate()
        },
    )
}

@Composable
private fun BillingRetentionScreen(
    isAnnualOfferShown: Boolean,
    onAnnualPlanClicked: () -> Unit,
    onManageSubscriptionClicked: () -> Unit,
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(24.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        BillingRetentionHeader(
            modifier = Modifier.fillMaxWidth(),
            onDismissClicked = onDismissClicked,
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(Res.string.billing_retention_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (isAnnualOfferShown) {
                BillingRetentionAnnualOffer(
                    modifier = Modifier.fillMaxWidth(),
                )
            }

            BillingRetentionPauseSection(
                modifier = Modifier.fillMaxWidth(),
            )

            BillingRetentionBenefitSection(
                modifier = Modifier.fillMaxWidth(),
            )
        }

        BillingRetentionButtonSection(
            modifier = Modifier.fillMaxWidth(),
            isAnnualOfferShown = isAnnualOfferShown,
            onAnnualPlanClicked = onAnnualPlanClicked,
            onManageSubscriptionClicked = onManageSubscriptionClicked,
            onDismissClicked = onDismissClicked,
        )
    }
}

@Composable
private fun BillingRetentionHeader(
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(Res.string.billing_retention_title),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface,
        )

        IconButton(
            onClick = onDismissClicked,
        ) {
            Icon(
                imageVector = Icons.Default.Close,
                contentDescription = stringResource(Res.string.common_close),
            )
        }
    }
}

@Composable
private fun BillingRetentionAnnualOffer(
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.primaryContainer,
    ) {
        BillingRetentionInfoRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            icon = Icons.Default.Savings,
            title = Res.string.billing_retention_annual_title,
            description = Res.string.billing_retention_annual_description,
        )
    }
}

@Composable
private fun BillingRetentionPauseSection(
    modifier: Modifier = Modifier,
) {
    BillingRetentionInfoRow(
        modifier = modifier,
        icon = Icons.Default.PauseCircle,
        title = Res.string.billing_retention_pause_title,
        description = Res.string.billing_retention_pause_description,
    )
}

@Composable
private fun BillingRetentionBenefitSection(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.billing_retention_benefits_title),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        BillingRetentionBenefitItem(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.DoNotDisturb,
            title = Res.string.billing_retention_benefit_hide_ads,
        )

        BillingRetentionBenefitItem(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.Download,
            title = Res.string.billing_retention_benefit_bulk_download,
        )

        BillingRetentionBenefitItem(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.GridView,
            title = Res.string.billing_retention_benefit_grid,
        )

        BillingRetentionBenefitItem(
            modifier = Modifier.fillMaxWidth(),
            icon = Icons.Default.VolunteerActivism,
            title = Res.string.billing_retention_benefit_support,
        )
    }
}

@Composable
private fun BillingRetentionInfoRow(
    icon: ImageVector,
    title: StringResource,
    description: StringResource,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(24.dp),
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(title),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun BillingRetentionBenefitItem(
    icon: ImageVector,
    title: StringResource,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(title),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}

@Composable
private fun BillingRetentionButtonSection(
    isAnnualOfferShown: Boolean,
    onAnnualPlanClicked: () -> Unit,
    onManageSubscriptionClicked: () -> Unit,
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (isAnnualOfferShown) {
            Button(
                modifier = Modifier.fillMaxWidth(),
                onClick = onAnnualPlanClicked,
            ) {
                Text(
                    text = stringResource(Res.string.billing_retention_primary_button),
                )
            }
        }

        OutlinedButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onManageSubscriptionClicked,
        ) {
            Text(
                text = stringResource(Res.string.billing_retention_manage_button),
            )
        }

        TextButton(
            modifier = Modifier.fillMaxWidth(),
            onClick = onDismissClicked,
        ) {
            Text(
                text = stringResource(Res.string.common_close),
            )
        }
    }
}

private fun Platform.subscriptionManagementUrl(): String {
    return when (this) {
        Platform.Android -> ANDROID_SUBSCRIPTION_MANAGEMENT_URL
        Platform.IOS -> IOS_SUBSCRIPTION_MANAGEMENT_URL
    }
}

/** リテンション BottomSheet から Plus 購入画面を開いたときの referrer。 */
private const val BILLING_RETENTION_REFERRER = "billing_retention"

/** 明示的に閉じるボタンを押したときの dismiss reason。 */
private const val BILLING_RETENTION_DISMISS_CLOSE_BUTTON = "close_button"

/** Play Store の PixiView 公開 applicationId 付き購読管理 URL。 */
private const val ANDROID_SUBSCRIPTION_MANAGEMENT_URL = "https://play.google.com/store/account/subscriptions?package=caios.android.fanbox"

/** App Store の標準購読管理 URL。 */
private const val IOS_SUBSCRIPTION_MANAGEMENT_URL = "https://apps.apple.com/account/subscriptions"
