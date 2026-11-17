package me.matsumo.fanbox.feature.library.home.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.OfferBanner
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_intro_offer_remaining_label
import me.matsumo.fanbox.core.resources.common_close
import me.matsumo.fanbox.core.resources.offer_banner_intro_first_month_title
import me.matsumo.fanbox.core.resources.offer_banner_intro_title
import me.matsumo.fanbox.core.resources.offer_banner_trial_title
import me.matsumo.fanbox.core.ui.extensition.getDisplayText
import me.matsumo.fanbox.core.ui.extensition.getTotalPeriodText
import me.matsumo.fanbox.core.ui.extensition.toCountdownText
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun LibraryHomeOfferBanner(
    banner: OfferBanner,
    onClick: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        onClick = onClick,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
        ),
    ) {
        Row(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 8.dp, end = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Redeem,
                contentDescription = null,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = banner.getTitle(),
                    style = MaterialTheme.typography.titleSmall,
                )

                if (banner is OfferBanner.IntroOffer) {
                    Text(
                        text = stringResource(Res.string.billing_plus_intro_offer_remaining_label, banner.remaining.toCountdownText()),
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }

            IconButton(onDismiss) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = stringResource(Res.string.common_close),
                )
            }
        }
    }
}

@Composable
private fun OfferBanner.getTitle(): String = when (this) {
    is OfferBanner.FreeTrial -> stringResource(Res.string.offer_banner_trial_title, trialPeriod.getDisplayText())

    is OfferBanner.IntroOffer -> if (introOffer.isFirstMonthOnly) {
        stringResource(Res.string.offer_banner_intro_first_month_title, introOffer.formattedPrice)
    } else {
        stringResource(Res.string.offer_banner_intro_title, introOffer.getTotalPeriodText(), introOffer.formattedPrice)
    }
}
