package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun TagItems(
    tags: ImmutableList<String>,
    onClickTag: (String) -> Unit,
    modifier: Modifier = Modifier,
    textStyle: TextStyle = MaterialTheme.typography.bodySmall,
    arrangementSpace: Dp = 8.dp,
) {
    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(arrangementSpace),
        verticalArrangement = Arrangement.spacedBy(arrangementSpace),
    ) {
        for (tag in tags) {
            TagItem(
                tag = tag,
                textStyle = textStyle,
                onClickTag = onClickTag,
            )
        }
    }
}

@Composable
private fun TagItem(
    tag: String,
    textStyle: TextStyle,
    onClickTag: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(4.dp))
            .clickable { onClickTag(tag) }
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                shape = RoundedCornerShape(4.dp),
            ),
    ) {
        Text(
            modifier = Modifier.padding(8.dp, 4.dp),
            text = tag,
            style = textStyle,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
