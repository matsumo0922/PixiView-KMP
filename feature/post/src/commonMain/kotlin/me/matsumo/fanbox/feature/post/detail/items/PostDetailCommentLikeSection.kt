package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
internal fun PostDetailCommentLikeButton(
    commentCount: Int,
    likeCount: Int,
    isBookmarked: Boolean,
    isLiked: Boolean,
    onClickLike: () -> Unit,
    onClickBookmark: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val likeColor = if (isLiked) Color(0xffe0405e) else MaterialTheme.colorScheme.onSurfaceVariant
    val bookmarkColor = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Filled.Comment,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = null,
            )

            Text(
                text = commentCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier
                .clip(CircleShape)
                .clickable {
                    if (!isLiked) {
                        onClickLike.invoke()
                    }
                    onClickBookmark.invoke(true)
                }
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Icon(
                modifier = Modifier.size(20.dp),
                imageVector = Icons.Default.Favorite,
                tint = likeColor,
                contentDescription = null,
            )

            Text(
                text = likeCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = likeColor,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        IconButton(
            onClick = { onClickBookmark.invoke(!isBookmarked) },
        ) {
            Icon(
                imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                tint = bookmarkColor,
                contentDescription = null,
            )
        }
    }
}
