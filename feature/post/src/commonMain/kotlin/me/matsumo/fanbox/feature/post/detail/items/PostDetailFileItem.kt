package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.common.util.toFileSizeString
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.common_download
import me.matsumo.fanbox.core.ui.component.video.VideoPlayer
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PostDetailFileItem(
    item: FanboxPostDetail.FileItem,
    onClickDownload: (FanboxPostDetail.FileItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    val videoItem = item.asVideoItem()

    Card(
        modifier = modifier.padding(16.dp),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    bottom = 8.dp,
                    start = 16.dp,
                    end = 16.dp,
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(
                space = 16.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "${item.name}.${item.extension}",
                style = MaterialTheme.typography.bodyLarge.bold().center(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (videoItem != null) {
                VideoPlayer(
                    modifier = Modifier.fillMaxWidth(),
                    url = videoItem.url,
                )
            }

            Button(onClick = { onClickDownload.invoke(item) }) {
                Text("${stringResource(Res.string.common_download)} (${item.size.toFloat().toFileSizeString()})")
            }
        }
    }
}
