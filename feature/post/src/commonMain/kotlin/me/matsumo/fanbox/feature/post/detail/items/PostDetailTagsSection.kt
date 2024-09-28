package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.matsumo.fanbox.core.ui.component.TagItems
import me.matsumo.fanbox.core.ui.extensition.padding

internal fun LazyListScope.postDetailTagsSection(
    tags: ImmutableList<String>,
    onClickTag: (String) -> Unit,
) {
    if (tags.isNotEmpty()) {
        item {
            TagItems(
                modifier = Modifier
                    .padding(horizontal = 16.dp, top = 16.dp)
                    .fillMaxWidth(),
                tags = tags.toImmutableList(),
                onClickTag = onClickTag,
            )
        }
    }
}
