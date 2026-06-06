package me.matsumo.fanbox.feature.library.notify.items

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import kotlinx.datetime.toStdlibInstant
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.im_default_user
import me.matsumo.fanbox.core.resources.notify_title_comment
import me.matsumo.fanbox.core.resources.notify_title_like
import me.matsumo.fanbox.core.resources.notify_title_post_published
import me.matsumo.fanbox.core.resources.unit_day_before
import me.matsumo.fanbox.core.resources.unit_hour_before
import me.matsumo.fanbox.core.resources.unit_minute_before
import me.matsumo.fanbox.core.resources.unit_second_before
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.asCoilImage
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fankt.fanbox.domain.model.FanboxBell
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
internal fun LibraryNotifyBellItem(
    bell: FanboxBell,
    onClickBell: (FanboxPostId) -> Unit,
    modifier: Modifier = Modifier,
) {
    when (bell) {
        is FanboxBell.Comment -> {
            CommentItem(
                modifier = modifier,
                bell = bell,
                onClickBell = onClickBell,
            )
        }
        is FanboxBell.Like -> {
            LikeItem(
                modifier = modifier,
                bell = bell,
                onClickBell = onClickBell,
            )
        }
        is FanboxBell.PostPublished -> {
            PostPublishedItem(
                modifier = modifier,
                bell = bell,
                onClickBell = onClickBell,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun PostPublishedItem(
    bell: FanboxBell.PostPublished,
    onClickBell: (FanboxPostId) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isShowCard = (bell.post.cover != null && bell.post.excerpt.isNotBlank())

    Row(
        modifier = modifier
            .clickable { onClickBell.invoke(bell.post.id) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = if (isShowCard) Alignment.Top else Alignment.CenterVertically,
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .error(Res.drawable.im_default_user.asCoilImage())
                .data(bell.post.user?.iconUrl)
                .build(),
            contentDescription = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.notify_title_post_published, bell.post.title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = bell.post.publishedDatetime.toRelativeTimeString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isShowCard) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 0.5.dp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            shape = RoundedCornerShape(4.dp),
                        )
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        modifier = Modifier.weight(5f),
                        text = bell.post.excerpt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis,
                    )

                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .weight(3f)
                            .aspectRatio(16 / 9f)
                            .clip(RoundedCornerShape(4.dp)),
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .fanboxHeader()
                            .data(bell.post.cover?.url)
                            .build(),
                        loading = {
                            FadePlaceHolder()
                        },
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class, ExperimentalTime::class)
@Composable
private fun CommentItem(
    bell: FanboxBell.Comment,
    onClickBell: (FanboxPostId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable { onClickBell.invoke(bell.postId) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .error(Res.drawable.im_default_user.asCoilImage())
                .data(bell.userProfileIconUrl)
                .build(),
            contentDescription = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.notify_title_comment, bell.userName),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = bell.notifiedDatetime.toRelativeTimeString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = bell.comment,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun LikeItem(
    bell: FanboxBell.Like,
    onClickBell: (FanboxPostId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clickable { onClickBell.invoke(bell.postId) }
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp),
            imageVector = Icons.Default.Favorite,
            tint = Color(0xffe0405e),
            contentDescription = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.notify_title_like, bell.count),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = bell.notifiedDatetime.toRelativeTimeString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = bell.comment,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun kotlinx.datetime.Instant.toRelativeTimeString(): String {
    val now = Clock.System.now()
    val duration = now - this.toStdlibInstant()

    return when {
        duration.inWholeDays > 0 -> stringResource(Res.string.unit_day_before, duration.inWholeDays)
        duration.inWholeHours > 0 -> stringResource(Res.string.unit_hour_before, duration.inWholeHours)
        duration.inWholeMinutes > 0 -> stringResource(Res.string.unit_minute_before, duration.inWholeMinutes)
        else -> stringResource(Res.string.unit_second_before, duration.inWholeSeconds)
    }
}
