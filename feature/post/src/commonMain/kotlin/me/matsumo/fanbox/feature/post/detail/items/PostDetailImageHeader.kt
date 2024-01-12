package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.ui.component.AdultContentThumbnail
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxMetadata

@Composable
internal fun PostDetailImageHeader(
    content: FanboxPostDetail.Body.Image,
    isAdultContents: Boolean,
    isOverrideAdultContents: Boolean,
    isTestUser: Boolean,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickDownload: (List<FanboxPostDetail.ImageItem>) -> Unit,
    modifier: Modifier = Modifier,
) {
    val metadata = LocalFanboxMetadata.current

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        for (item in content.images) {
            if (!isOverrideAdultContents && !metadata.context.user.showAdultContent && isAdultContents) {
                AdultContentThumbnail(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(item.aspectRatio),
                    coverImageUrl = item.thumbnailUrl,
                    isTestUser = isTestUser,
                )
            } else {
                PostDetailImageItem(
                    modifier = Modifier.fillMaxWidth(),
                    item = item,
                    onClickImage = onClickImage,
                    onClickDownload = { onClickDownload.invoke(listOf(item)) },
                    onClickAllDownload = { onClickDownload.invoke(content.imageItems) },
                )
            }
        }

        if (content.text.isNotBlank()) {
            Text(
                modifier = Modifier.padding(16.dp),
                text = content.text,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }
    }
}
