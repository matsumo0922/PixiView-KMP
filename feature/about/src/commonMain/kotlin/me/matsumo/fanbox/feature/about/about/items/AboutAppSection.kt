package me.matsumo.fanbox.feature.about.about.items

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.icon.Discord
import me.matsumo.fanbox.core.ui.icon.GitHub
import me.matsumo.fanbox.core.ui.icon.GooglePlay

@Composable
internal fun AboutAppSection(
    userData: UserData,
    config: PixiViewConfig,
    onClickGithub: () -> Unit,
    onClickDiscord: () -> Unit,
    onClickGooglePlay: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(6.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp, 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(24.dp),
            ) {
                Image(
                    modifier = Modifier
                        .size(104.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    painter = painterResource(MR.images.ic_app_icon),
                    contentDescription = null,
                )

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = stringResource(MR.strings.about_name),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Text(
                        modifier = Modifier.fillMaxWidth(),
                        text = "${config.versionName}:${config.versionCode}" + when {
                            userData.isPlusMode && userData.isDeveloperMode -> " [P+D]"
                            userData.isPlusMode -> " [Premium]"
                            userData.isDeveloperMode -> " [Developer]"
                            else -> ""
                        } + if (userData.isTestUser) " [Test]" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Row(
                        modifier = Modifier
                            .padding(top = 12.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                    ) {
                        AboutIconButton(
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Outlined.GitHub,
                            onClick = { onClickGithub.invoke() },
                        )

                        AboutIconButton(
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Outlined.Discord,
                            onClick = { onClickDiscord.invoke() },
                        )

                        AboutIconButton(
                            modifier = Modifier.size(28.dp),
                            imageVector = Icons.Outlined.GooglePlay,
                            onClick = { onClickGooglePlay.invoke() },
                        )
                    }
                }
            }
        }
    }
}
