package me.matsumo.fanbox.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.PixiViewNavHost
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation.decelerateEasing
import me.matsumo.fanbox.core.ui.post_detail_downloading
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PixiViewContent(
    downloadState: DownloadState,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        PixiViewNavHost(
            modifier = Modifier.fillMaxSize(),
        )

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth(),
            visible = downloadState is DownloadState.Downloading,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
            exit = fadeOut(),
        ) {
            DownloadingItem(
                modifier = Modifier
                    .navigationBarsPadding()
                    .fillMaxWidth(),
                downloading = downloadState as? DownloadState.Downloading,
            )
        }
    }
}

@Composable
private fun DownloadingItem(
    downloading: DownloadState.Downloading?,
    modifier: Modifier = Modifier,
) {
    val progress by animateFloatAsState(
        targetValue = downloading?.progress ?: 1f,
        animationSpec = tween(500, 0, decelerateEasing),
        label = "Downloading progress",
    )

    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(3.dp)),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.post_detail_downloading),
                    style = MaterialTheme.typography.titleSmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "${(progress * 100).toInt()} %",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = downloading?.title.orEmpty(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            LinearProgressIndicator(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                progress = { progress },
            )
        }
    }
}
