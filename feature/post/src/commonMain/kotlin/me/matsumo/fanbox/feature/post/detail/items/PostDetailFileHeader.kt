package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import sh.calvin.autolinktext.AutoLinkText
import sh.calvin.autolinktext.TextRuleDefaults

internal fun LazyListScope.postDetailFileHeader(
    isAutoImagePreview: Boolean,
    content: FanboxPostDetail.Body.File,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickFile: (FanboxPostDetail.FileItem) -> Unit,
    onClickDownload: (List<FanboxPostDetail.ImageItem>) -> Unit,
) {
    items(content.files) {
        val imageItem = it.asImageItem()

        if (isAutoImagePreview && imageItem != null) {
            PostDetailImageItem(
                modifier = Modifier.fillMaxWidth(),
                item = imageItem,
                onClickImage = onClickImage,
                onClickDownload = { onClickDownload.invoke(listOf(imageItem)) },
                onClickAllDownload = { onClickDownload.invoke(content.imageItems) },
            )
        } else {
            PostDetailFileItem(
                modifier = Modifier.fillMaxWidth(),
                item = it,
                onClickDownload = onClickFile,
            )
        }
    }

    if (content.text.isNotBlank()) {
        item {
            AutoLinkText(
                modifier = Modifier.padding(16.dp),
                text = content.text,
                style = MaterialTheme.typography.bodyMedium.copy(color = MaterialTheme.colorScheme.onSurface),
                textRules = TextRuleDefaults.defaultList().map {
                    it.copy(
                        style = SpanStyle(
                            color = MaterialTheme.colorScheme.primary,
                            textDecoration = TextDecoration.Underline,
                        ),
                    )
                },
            )
        }
    }
}
