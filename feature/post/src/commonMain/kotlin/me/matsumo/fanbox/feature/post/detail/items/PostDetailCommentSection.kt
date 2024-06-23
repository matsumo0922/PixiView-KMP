package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Message
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.CommentId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.common_delete
import me.matsumo.fanbox.core.ui.common_see_more
import me.matsumo.fanbox.core.ui.extensition.asCoilImage
import me.matsumo.fanbox.core.ui.extensition.padding
import me.matsumo.fanbox.core.ui.im_default_user
import me.matsumo.fanbox.core.ui.post_detail_comment_empty
import me.matsumo.fanbox.core.ui.post_detail_comment_reply
import me.matsumo.fanbox.core.ui.post_detail_comment_title
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import me.matsumo.fanbox.core.ui.unit_day_before
import me.matsumo.fanbox.core.ui.unit_hour_before
import me.matsumo.fanbox.core.ui.unit_minute_before
import me.matsumo.fanbox.core.ui.unit_second_before
import org.jetbrains.compose.resources.stringResource

internal fun LazyListScope.postDetailCommentItems(
    isShowCommentEditor: Boolean,
    postDetail: FanboxPostDetail,
    metaData: FanboxMetaData,
    onClickLoadMore: (PostId, Int) -> Unit,
    onClickCommentLike: (CommentId) -> Unit,
    onClickCommentReply: (String, CommentId, CommentId) -> Unit,
    onClickCommentDelete: (CommentId) -> Unit,
    onClickShowCommentEditor: (Boolean) -> Unit,
) {
    item {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp, top = 16.dp)
                .fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.post_detail_comment_title),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                IconButton(onClick = { onClickShowCommentEditor.invoke(!isShowCommentEditor) }) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        tint = MaterialTheme.colorScheme.onSurface,
                        contentDescription = null,
                    )
                }
            }

            AnimatedVisibility(visible = isShowCommentEditor) {
                CommentEditor(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(top = 24.dp)
                        .padding(horizontal = 8.dp)
                        .fillMaxWidth(),
                    parentCommentId = CommentId("0"),
                    rootCommentId = CommentId("0"),
                    metaData = metaData,
                    onClickCommentReply = { body, parentCommentId, rootCommentId ->
                        onClickCommentReply.invoke(body, parentCommentId, rootCommentId)
                    },
                )
            }
        }
    }

    if (postDetail.commentList.contents.isNotEmpty()) {
        items(
            items = postDetail.commentList.contents,
            key = { comment -> comment.id.uniqueValue },
        ) {
            CommentItem(
                modifier = Modifier
                    .padding(horizontal = 24.dp, top = 16.dp)
                    .fillMaxWidth(),
                metaData = metaData,
                comment = it,
                onClickCommentLike = onClickCommentLike,
                onClickCommentReply = { body, parentCommentId, rootCommentId ->
                    onClickCommentReply.invoke(body, parentCommentId, rootCommentId)
                },
                onClickCommentDelete = onClickCommentDelete,
            )
        }
    } else {
        item {
            Text(
                modifier = Modifier
                    .alpha(if (!isShowCommentEditor) 1f else 0f)
                    .padding(
                        top = 24.dp,
                        start = 16.dp,
                        end = 16.dp,
                    )
                    .fillMaxWidth(),
                text = stringResource(Res.string.post_detail_comment_empty),
                style = MaterialTheme.typography.bodyMedium.center(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }

    if (postDetail.commentList.offset != null) {
        item {
            Box(
                modifier = Modifier
                    .padding(horizontal = 16.dp, top = 16.dp)
                    .fillMaxWidth()
            ) {
                Button(
                    modifier = Modifier
                        .padding(top = 24.dp)
                        .align(Alignment.Center),
                    onClick = { onClickLoadMore.invoke(postDetail.id, postDetail.commentList.offset!!) },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            contentDescription = null,
                        )

                        Text(
                            modifier = Modifier.padding(start = 8.dp),
                            text = stringResource(Res.string.common_see_more),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun CommentItem(
    comment: FanboxPostDetail.Comment.CommentItem,
    metaData: FanboxMetaData,
    onClickCommentLike: (CommentId) -> Unit,
    onClickCommentReply: (String, CommentId, CommentId) -> Unit,
    onClickCommentDelete: (CommentId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isShowReplyEditor by rememberSaveable(comment) { mutableStateOf(false) }
    var isLiked by rememberSaveable { mutableStateOf(comment.isLiked) }
    val likeColor = if (isLiked) Color(0xffe0405e) else MaterialTheme.colorScheme.onSurfaceVariant

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .error(Res.drawable.im_default_user.asCoilImage())
                .data(comment.user.iconUrl)
                .build(),
            contentDescription = null,
        )

        Column(
            modifier = Modifier
                .animateContentSize()
                .fillMaxWidth(),
        ) {
            Text(
                text = comment.user.name,
                style = MaterialTheme.typography.bodyMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                modifier = Modifier.padding(top = 8.dp),
                text = comment.body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = comment.createdDatetime.toRelativeTimeString(),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable {
                            if (!isLiked) {
                                isLiked = true
                                onClickCommentLike.invoke(comment.id)
                            }
                        }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.Favorite,
                        tint = likeColor,
                        contentDescription = null,
                    )

                    Text(
                        text = (comment.likeCount + if (isLiked) 1 else 0).toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = likeColor,
                    )
                }

                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .clickable { isShowReplyEditor = !isShowReplyEditor }
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Icon(
                        modifier = Modifier.size(16.dp),
                        imageVector = Icons.Default.Message,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        contentDescription = null,
                    )

                    Text(
                        text = stringResource(Res.string.post_detail_comment_reply),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }

                if (comment.isOwn) {
                    Row(
                        modifier = Modifier
                            .clip(CircleShape)
                            .clickable { onClickCommentDelete.invoke(comment.id) }
                            .padding(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Icon(
                            modifier = Modifier.size(16.dp),
                            imageVector = Icons.Default.Delete,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            contentDescription = null,
                        )

                        Text(
                            text = stringResource(Res.string.common_delete),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            for (reply in comment.replies) {
                CommentItem(
                    modifier = Modifier
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    metaData = metaData,
                    comment = reply,
                    onClickCommentLike = onClickCommentLike,
                    onClickCommentReply = onClickCommentReply,
                    onClickCommentDelete = onClickCommentDelete,
                )
            }

            AnimatedVisibility(visible = isShowReplyEditor) {
                CommentEditor(
                    modifier = Modifier
                        .animateContentSize()
                        .padding(top = 8.dp)
                        .fillMaxWidth(),
                    parentCommentId = comment.id,
                    rootCommentId = comment.rootCommentId,
                    metaData = metaData,
                    onClickCommentReply = onClickCommentReply,
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun CommentEditor(
    parentCommentId: CommentId,
    rootCommentId: CommentId,
    metaData: FanboxMetaData,
    onClickCommentReply: (String, CommentId, CommentId) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isError by rememberSaveable { mutableStateOf(false) }
    var value by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(value) {
        isError = value.length > 1000
    }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .error(Res.drawable.im_default_user.asCoilImage())
                .data(metaData.context.user.iconUrl)
                .build(),
            contentDescription = null,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = value,
                onValueChange = { value = it },
                isError = isError,
                supportingText = {
                    Text("${value.length} / 1000")
                },
            )

            Button(
                modifier = Modifier.align(Alignment.End),
                enabled = !isError,
                onClick = {
                    onClickCommentReply.invoke(value, parentCommentId, if (rootCommentId.value != "0") rootCommentId else parentCommentId)
                },
            ) {
                Text(text = stringResource(Res.string.post_detail_comment_reply))
            }
        }
    }
}

@Composable
private fun Instant.toRelativeTimeString(): String {
    val now = Clock.System.now()
    val duration = now - this

    return when {
        duration.inWholeDays > 0 -> stringResource(Res.string.unit_day_before, duration.inWholeDays)
        duration.inWholeHours > 0 -> stringResource(Res.string.unit_hour_before, duration.inWholeHours)
        duration.inWholeMinutes > 0 -> stringResource(Res.string.unit_minute_before, duration.inWholeMinutes)
        else -> stringResource(Res.string.unit_second_before, duration.inWholeSeconds)
    }
}
