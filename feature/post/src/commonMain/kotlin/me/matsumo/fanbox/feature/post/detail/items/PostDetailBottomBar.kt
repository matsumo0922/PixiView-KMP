package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.im_default_user
import me.matsumo.fanbox.core.ui.extensition.asCoilImage
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

@Composable
internal fun PostDetailBottomBar(
    postDetail: FanboxPostDetail?,
    isBookmarked: Boolean,
    onCreatorClicked: (FanboxCreatorId) -> Unit,
    onBookmarkClicked: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val bookmarkColor = if (isBookmarked) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant

    AnimatedContent(
        modifier = modifier
            .navigationBarsPadding()
            .padding(16.dp, 8.dp),
        targetState = postDetail,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            AsyncImage(
                modifier = Modifier
                    .clip(CircleShape)
                    .size(36.dp)
                    .clickable { it?.user?.creatorId?.let(onCreatorClicked) },
                model = ImageRequest.Builder(LocalPlatformContext.current).error(Res.drawable.im_default_user.asCoilImage())
                    .data(it?.user?.iconUrl).build(),
                contentDescription = null,
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it?.title.orEmpty(),
                    style = MaterialTheme.typography.bodyLarge.bold(),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = it?.user?.name.orEmpty(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            IconButton(
                onClick = { onBookmarkClicked.invoke(!isBookmarked) },
            ) {
                Icon(
                    imageVector = if (isBookmarked) Icons.Default.Bookmark else Icons.Outlined.BookmarkBorder,
                    tint = bookmarkColor,
                    contentDescription = null,
                )
            }
        }
    }
}
