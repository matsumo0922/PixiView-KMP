package me.matsumo.fanbox.feature.about.billing

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
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

@OptIn(ExperimentalComposeUiApi::class)
@Composable
internal fun BillingRetentionRoute(
    isAnnualOfferShown: Boolean,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    navigatorExtension: NavigatorExtension = koinInject(),
) {
    val navController = LocalNavController.current

    BackHandler {
        dismissBillingRetentionPrompt(
            reason = BILLING_RETENTION_DISMISS_BACK_BUTTON,
            terminate = terminate,
        )
    }

    BillingRetentionScreen(
        modifier = modifier,
        isAnnualOfferShown = isAnnualOfferShown,
        onAnnualPlanClicked = {
            BillingLog.retentionPromptAnnualClicked().send()
            navController.navigate(
                Destination.BillingPlusBottomSheet(
                    referrer = BILLING_RETENTION_REFERRER,
                    initialPlanType = BillingPlan.Type.ANNUAL,
                ),
            ) {
                popUpTo<Destination.BillingRetentionBottomSheet> {
                    inclusive = true
                }
            }
        },
        onManageSubscriptionClicked = {
            val subscriptionManagementUrl = currentPlatform.subscriptionManagementUrl()

            BillingLog.retentionPromptManageClicked(currentPlatform.name).send()
            terminate()
            navigatorExtension.navigateToWebPage(
                url = subscriptionManagementUrl,
                referrer = BILLING_RETENTION_REFERRER,
            )
        },
        onDismissClicked = {
            dismissBillingRetentionPrompt(
                reason = BILLING_RETENTION_DISMISS_CLOSE_BUTTON,
                terminate = terminate,
            )
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
    Scaffold(
        modifier = modifier,
        topBar = {
            BillingRetentionHeader(
                modifier = Modifier
                    .statusBarsPadding()
                    .fillMaxWidth(),
                onDismissClicked = onDismissClicked,
            )
        },
        bottomBar = {
            BillingRetentionButtonSection(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth(),
                isAnnualOfferShown = isAnnualOfferShown,
                onAnnualPlanClicked = onAnnualPlanClicked,
                onManageSubscriptionClicked = onManageSubscriptionClicked,
            )
        },
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(it)
                .padding(24.dp)
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
                BillingRetentionInfoRow(
                    modifier = Modifier.fillMaxWidth(),
                    icon = Icons.Default.Savings,
                    title = Res.string.billing_retention_annual_title,
                    description = Res.string.billing_retention_annual_description,
                )
            }

            BillingRetentionInfoRow(
                modifier = Modifier.fillMaxWidth(),
                icon = Icons.Default.PauseCircle,
                title = Res.string.billing_retention_pause_title,
                description = Res.string.billing_retention_pause_description,
            )
        }
    }
}

@Composable
private fun BillingRetentionHeader(
    onDismissClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 24.dp),
        verticalAlignment = Alignment.CenterVertically,
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
private fun BillingRetentionButtonSection(
    isAnnualOfferShown: Boolean,
    onAnnualPlanClicked: () -> Unit,
    onManageSubscriptionClicked: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.padding(horizontal = 24.dp),
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
    }
}

private fun Platform.subscriptionManagementUrl(): String {
    return when (this) {
        Platform.Android -> ANDROID_SUBSCRIPTION_MANAGEMENT_URL
        Platform.IOS -> IOS_SUBSCRIPTION_MANAGEMENT_URL
    }
}

/** リテンション BottomSheet を理由付きで閉じる。 */
private fun dismissBillingRetentionPrompt(reason: String, terminate: () -> Unit) {
    BillingLog.retentionPromptDismissed(reason).send()
    terminate()
}

/** リテンション BottomSheet から Plus 購入画面を開いたときの referrer。 */
private const val BILLING_RETENTION_REFERRER = "billing_retention"

/** 明示的に閉じるボタンを押したときの dismiss reason。 */
private const val BILLING_RETENTION_DISMISS_CLOSE_BUTTON = "close_button"

/** システム戻る操作で閉じたときの dismiss reason。 */
private const val BILLING_RETENTION_DISMISS_BACK_BUTTON = "back"

/** Play Store の PixiView 公開 applicationId 付き購読管理 URL。 */
private const val ANDROID_SUBSCRIPTION_MANAGEMENT_URL = "https://play.google.com/store/account/subscriptions?package=caios.android.fanbox"

/** App Store の標準購読管理 URL。 */
private const val IOS_SUBSCRIPTION_MANAGEMENT_URL = "https://apps.apple.com/account/subscriptions"
