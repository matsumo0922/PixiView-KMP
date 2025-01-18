package me.matsumo.fanbox.feature.creator.top.items

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.creator_download_require_plus_button
import me.matsumo.fanbox.core.resources.creator_download_require_plus_button_ad
import me.matsumo.fanbox.core.resources.creator_download_require_plus_button_ad_over
import me.matsumo.fanbox.core.resources.creator_download_require_plus_message
import me.matsumo.fanbox.core.resources.creator_download_require_plus_title
import me.matsumo.fanbox.core.ui.ads.RewardAdLoader
import me.matsumo.fanbox.core.ui.appName
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject

@Suppress("UnstableCollections", "ModifierMissing")
@Composable
actual fun CreatorTopRewardAdDialog(
    isAbleToReward: Boolean,
    onRewarded: () -> Unit,
    onClickShowPlus: () -> Unit,
    onDismissRequest: () -> Unit,
) {
    val activity = LocalActivity.current as FragmentActivity

    val rewardAdLoader = koinInject<RewardAdLoader>()
    val rewardAd by rewardAdLoader.rewardAd.collectAsStateWithLifecycle()

    if (rewardAd == null) {
        LaunchedEffect(true) {
            rewardAdLoader.loadRewardAdIfNeeded()
        }
    }

    Dialog(onDismissRequest = onDismissRequest) {
        Column(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surface)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 8.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.creator_download_require_plus_title),
                    style = MaterialTheme.typography.titleMedium.bold(),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(Res.string.creator_download_require_plus_message, appName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Column(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {
                    Button(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = onClickShowPlus,
                    ) {
                        Text(text = stringResource(Res.string.creator_download_require_plus_button, appName))
                    }

                    OutlinedButton(
                        modifier = Modifier.fillMaxWidth(),
                        onClick = {
                            rewardAd?.show(activity) {
                                onRewarded.invoke()
                            }
                        },
                        enabled = rewardAd != null && isAbleToReward,
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            if (rewardAd == null) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp,
                                )
                            }

                            Text(text = stringResource(if (isAbleToReward) Res.string.creator_download_require_plus_button_ad else Res.string.creator_download_require_plus_button_ad_over))
                        }
                    }
                }
            }
        }
    }
}
