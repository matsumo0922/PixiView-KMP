package me.matsumo.fanbox.feature.about.about.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Feedback
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.Redeem
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.about_support
import me.matsumo.fanbox.core.ui.about_support_donate
import me.matsumo.fanbox.core.ui.about_support_opinion
import me.matsumo.fanbox.core.ui.about_support_rate_app
import me.matsumo.fanbox.core.ui.about_support_version_history
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun AboutSupportSection(
    onClickVersionHistory: () -> Unit,
    onClickRateApp: () -> Unit,
    onClickEmail: () -> Unit,
    onClickDonation: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(),
            ) {
                Text(
                    modifier = Modifier
                        .padding(
                            top = 24.dp,
                            start = 24.dp,
                            end = 24.dp,
                            bottom = 18.dp,
                        )
                        .fillMaxWidth(),
                    text = stringResource(Res.string.about_support).uppercase(),
                    style = MaterialTheme.typography.bodyMedium.bold(),
                    color = MaterialTheme.colorScheme.primary,
                )

                AboutSupportItem(
                    modifier = Modifier.fillMaxWidth(),
                    titleRes = Res.string.about_support_version_history,
                    imageVector = Icons.Outlined.History,
                    onClick = onClickVersionHistory,
                )

                AboutSupportItem(
                    modifier = Modifier.fillMaxWidth(),
                    titleRes = Res.string.about_support_rate_app,
                    imageVector = Icons.Outlined.StarOutline,
                    onClick = onClickRateApp,
                )

                AboutSupportItem(
                    modifier = Modifier.fillMaxWidth(),
                    titleRes = Res.string.about_support_opinion,
                    imageVector = Icons.Outlined.Feedback,
                    onClick = onClickEmail,
                )

                AboutSupportItem(
                    modifier = Modifier
                        .padding(bottom = 16.dp)
                        .fillMaxWidth(),
                    titleRes = Res.string.about_support_donate,
                    imageVector = Icons.Outlined.Redeem,
                    onClick = onClickDonation,
                )
            }
        }
    }
}

@Composable
private fun AboutSupportItem(
    titleRes: StringResource,
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable { onClick.invoke() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier.size(20.dp),
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Text(
            modifier = Modifier.weight(1f),
            text = stringResource(titleRes),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )
    }
}
