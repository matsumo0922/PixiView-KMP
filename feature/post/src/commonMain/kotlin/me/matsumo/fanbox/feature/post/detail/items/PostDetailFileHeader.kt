package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail

@Composable
internal fun PostDetailFileHeader(
    content: FanboxPostDetail.Body.File,
    onClickFile: (FanboxPostDetail.FileItem) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        for (item in content.files) {
            PostDetailFileItem(
                modifier = Modifier.fillMaxWidth(),
                item = item,
                onClickDownload = onClickFile,
            )
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
