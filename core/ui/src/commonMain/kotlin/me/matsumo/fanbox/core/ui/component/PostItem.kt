package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.outlined.BookmarkBorder
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.error_restricted_post
import me.matsumo.fanbox.core.ui.error_restricted_post_ios
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.asCoilImage
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.fanbox_free_fee
import me.matsumo.fanbox.core.ui.im_default_user
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import me.matsumo.fanbox.core.ui.unit_jpy
import org.jetbrains.compose.resources.stringResource

@Composable
fun PostItem(
    post: FanboxPost,
    onClickPost: (PostId) -> Unit,
    onClickCreator: (CreatorId) -> Unit,
    onClickPlanList: (CreatorId) -> Unit,
    onClickLike: (PostId) -> Unit,
    onClickBookmark: (PostId, Boolean) -> Unit,
    isHideAdultContents: Boolean,
    isOverrideAdultContents: Boolean,
    isTestUser: Boolean,
    modifier: Modifier = Modifier,
) {
    var isPostBookmarked by remember(post.isBookmarked) { mutableStateOf(post.isBookmarked) }
    var isPostLiked by rememberSaveable(post.isLiked) { mutableStateOf(post.isLiked) }
    var isHideAdultContent by remember { mutableStateOf(isHideAdultContents) }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClickPost.invoke(post.id) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            when {
                post.isRestricted -> {
                    RestrictThumbnail(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9),
                        feeRequired = post.feeRequired,
                    )
                }

                post.cover == null -> {
                    FileThumbnail(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9),
                    )
                }

                post.hasAdultContent && (isHideAdultContent || !isOverrideAdultContents) -> {
                    AdultContentThumbnail(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9),
                        coverImageUrl = post.cover?.url,
                        isAllowedShow = isOverrideAdultContents,
                        isTestUser = isTestUser,
                        onClickShowAdultContent = { isHideAdultContent = false },
                    )
                }

                else -> {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(16f / 9),
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .fanboxHeader()
                            .data(post.cover?.url)
                            .build(),
                        onError = {
                            Napier.e("error: ${it.result.throwable}")
                        },
                        contentScale = ContentScale.Crop,
                        contentDescription = null,
                    )
                }
            }

            UserSection(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                post = post,
                onClickCreator = onClickCreator,
            )

            Text(
                modifier = Modifier
                    .padding(horizontal = 16.dp, vertical = 8.dp)
                    .fillMaxWidth(),
                text = post.title,
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )

            if (post.isRestricted) {
                RestrictCardItem(
                    modifier = Modifier
                        .padding(
                            top = 8.dp,
                            start = 16.dp,
                            end = 16.dp,
                            bottom = 16.dp,
                        )
                        .fillMaxWidth(),
                    backgroundColor = MaterialTheme.colorScheme.secondaryContainer,
                    feeRequired = post.feeRequired,
                    onClickPlanList = { onClickPlanList.invoke(post.user.creatorId) },
                )
            } else {
                if (post.excerpt.isNotBlank()) {
                    Text(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        text = post.excerpt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                    )
                }

                CommentLikeButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(
                            start = 16.dp,
                            end = 8.dp,
                            bottom = 8.dp,
                        ),
                    commentCount = post.commentCount,
                    likeCount = post.likeCount + if (isPostLiked) 1 else 0,
                    isBookmarked = isPostBookmarked,
                    isLiked = isPostLiked,
                    onClickLike = {
                        isPostLiked = true
                        onClickLike.invoke(post.id)
                    },
                    onClickBookmark = {
                        isPostBookmarked = it
                        onClickBookmark.invoke(post.id, it)
                    },
                )
            }
        }
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun UserSection(
    post: FanboxPost,
    onClickCreator: (CreatorId) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Row(
            modifier = Modifier
                .weight(1f)
                .clip(RoundedCornerShape(4.dp))
                .clickable { onClickCreator.invoke(post.user.creatorId) }
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            SubcomposeAsyncImage(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape),
                model = ImageRequest.Builder(LocalPlatformContext.current)
                    .error(Res.drawable.im_default_user.asCoilImage())
                    .data(post.user.iconUrl)
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
                    text = post.user.name,
                    style = MaterialTheme.typography.bodyMedium.bold(),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = post.publishedDatetime.format("yyyy/MM/dd HH:mm"),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Text(
                modifier = Modifier.padding(6.dp, 4.dp),
                text = if (post.feeRequired == 0) stringResource(Res.string.fanbox_free_fee) else stringResource(Res.string.unit_jpy, post.feeRequired),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun RestrictThumbnail(
    feeRequired: Int,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.Default.Lock,
            tint = Color.White,
            contentDescription = null,
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = if (currentPlatform == Platform.Android) stringResource(Res.string.error_restricted_post, feeRequired) else stringResource(Res.string.error_restricted_post_ios),
            style = MaterialTheme.typography.bodySmall.center(),
            color = Color.White,
        )
    }
}

@Composable
private fun FileThumbnail(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.DarkGray)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(
            space = 16.dp,
            alignment = Alignment.CenterVertically,
        ),
    ) {
        Icon(
            modifier = Modifier.size(48.dp),
            imageVector = Icons.Filled.InsertDriveFile,
            tint = Color.White,
            contentDescription = null,
        )
    }
}

@Composable
private fun CommentLikeButton(
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
