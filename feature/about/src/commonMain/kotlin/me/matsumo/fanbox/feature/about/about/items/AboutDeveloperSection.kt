package me.matsumo.fanbox.feature.about.about.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.layoutId
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.icon.GitHub
import me.matsumo.fanbox.core.ui.icon.GooglePlay
import me.matsumo.fanbox.core.ui.icon.Twitter
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center

@Composable
internal fun AboutDeveloperSection(
    onClickTwitter: () -> Unit,
    onClickGithub: () -> Unit,
    onClickGooglePlay: () -> Unit,
    onClickGitHubContributor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Layout(
        modifier = modifier,
        content = {
            ProfileIconItem(
                modifier = Modifier.layoutId("icon")
            )

            AboutDeveloperCard(
                modifier = Modifier.layoutId("card"),
                onClickTwitter = onClickTwitter,
                onClickGithub = onClickGithub,
                onClickGooglePlay = onClickGooglePlay,
                onClickGitHubContributor = onClickGitHubContributor,
            )
        },
        measurePolicy = { measurables, constraints ->
            val icon = measurables.find { it.layoutId == "icon" }!!
            val card = measurables.find { it.layoutId == "card" }!!

            val iconPlaceable = icon.measure(Constraints.fixed(96.dp.roundToPx(), 96.dp.roundToPx()))
            val cardPlaceable = card.measure(constraints)

            val iconPosition = Alignment.TopCenter.align(
                size = IntSize(iconPlaceable.width, iconPlaceable.height),
                space = IntSize(constraints.maxWidth, constraints.maxHeight),
                layoutDirection,
            )

            val cardPosition = Alignment.TopCenter.align(
                size = IntSize(cardPlaceable.width, cardPlaceable.height),
                space = IntSize(constraints.maxWidth, constraints.maxHeight),
                layoutDirection,
            ).copy(
                y = iconPosition.y + iconPlaceable.height / 2,
            )

            layout(constraints.maxWidth, cardPlaceable.height + cardPosition.y) {
                cardPlaceable.place(cardPosition)
                iconPlaceable.place(iconPosition)
            }
        },
    )
}

@Composable
private fun ProfileIconItem(
    modifier: Modifier = Modifier,
) {
    Image(
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 1.dp,
                color = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
            ),
        painter = painterResource(MR.images.ic_developer_profile),
        contentDescription = null,
    )
}

@Composable
private fun AboutDeveloperCard(
    onClickTwitter: () -> Unit,
    onClickGithub: () -> Unit,
    onClickGooglePlay: () -> Unit,
    onClickGitHubContributor: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(6.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                modifier = Modifier
                    .padding(top = 68.dp)
                    .fillMaxWidth(),
                text = stringResource(MR.strings.about_developer_prefix),
                style = MaterialTheme.typography.bodyMedium.center(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Text(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                text = stringResource(MR.strings.about_developer_name),
                style = MaterialTheme.typography.titleLarge.center(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier
                    .padding(top = 24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(
                    space = 16.dp,
                    alignment = Alignment.CenterHorizontally,
                ),
            ) {
                AboutIconButton(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Outlined.Twitter,
                    onClick = onClickTwitter,
                )

                AboutIconButton(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Outlined.GitHub,
                    onClick = onClickGithub,
                )

                AboutIconButton(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Outlined.GooglePlay,
                    onClick = onClickGooglePlay,
                )
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 24.dp)
                    .fillMaxWidth(),
                text = stringResource(MR.strings.about_special_thanks).uppercase(),
                style = MaterialTheme.typography.bodyMedium.bold(),
                color = MaterialTheme.colorScheme.primary,
            )

            AboutThanksItem(
                modifier = Modifier.fillMaxWidth(),
                titleRes = MR.strings.about_special_thanks_contributor,
                descriptionRes = MR.strings.about_special_thanks_contributor_description,
                iconVector = Icons.Outlined.GitHub,
            ) {
                AboutIconButton(
                    modifier = Modifier.size(28.dp),
                    imageVector = Icons.Outlined.GitHub,
                    onClick = onClickGitHubContributor,
                )
            }

            Divider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp, vertical = 8.dp),
            )

            Text(
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        start = 24.dp,
                        end = 24.dp,
                    )
                    .fillMaxWidth(),
                text = stringResource(MR.strings.about_contribute).uppercase(),
                style = MaterialTheme.typography.bodyMedium.bold(),
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        bottom = 24.dp,
                        start = 24.dp,
                        end = 24.dp,
                    )
                    .fillMaxWidth(),
                text = stringResource(MR.strings.about_contribute_description),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            if (currentPlatform != Platform.Android) {
                Text(
                    modifier = Modifier
                        .padding(
                            bottom = 24.dp,
                            start = 24.dp,
                            end = 24.dp,
                        )
                        .fillMaxWidth(),
                    text = stringResource(MR.strings.about_contribute_ios_description),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}
