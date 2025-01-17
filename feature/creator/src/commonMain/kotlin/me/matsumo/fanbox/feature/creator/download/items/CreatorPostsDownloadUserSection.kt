package me.matsumo.fanbox.feature.creator.download.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.im_default_user
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.asCoilImage
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail

@Composable
internal fun CreatorPostsDownloadUserSection(
    creatorDetail: FanboxCreatorDetail,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .error(Res.drawable.im_default_user.asCoilImage())
                .data(creatorDetail.user?.iconUrl)
                .build(),
            loading = {
                FadePlaceHolder()
            },
            contentDescription = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = creatorDetail.user?.name.orEmpty(),
                style = MaterialTheme.typography.bodyLarge.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "@${creatorDetail.user?.creatorId?.value ?: "Unknown"}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
