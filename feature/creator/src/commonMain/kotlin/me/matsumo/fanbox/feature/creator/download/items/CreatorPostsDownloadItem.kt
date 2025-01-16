package me.matsumo.fanbox.feature.creator.download.items

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.fanbox_free_fee
import me.matsumo.fanbox.core.resources.unit_jpy
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.feature.creator.download.CreatorPostsDownloadData
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import org.jetbrains.compose.resources.stringResource
import kotlin.random.Random

@Composable
internal fun CreatorPostsDownloadItem(
    data: CreatorPostsDownloadData,
    isTarget: Boolean,
    isDownloaded: Boolean,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp),
        ) {
            if (data.post.cover == null) {
                FileThumbnail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                )
            } else {
                CoverThumbnail(
                    modifier = Modifier
                        .fillMaxHeight()
                        .aspectRatio(1f),
                    url = data.post.cover?.url,
                    isDownloaded = isDownloaded,
                )
            }

            Column(
                modifier = Modifier
                    .padding(
                        top = 16.dp,
                        start = 16.dp,
                        end = 16.dp,
                        bottom = 8.dp,
                    )
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = data.post.title,
                    style = MaterialTheme.typography.titleMedium.bold(),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )

                if (data.post.excerpt.isNotBlank()) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        text = data.post.excerpt,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        overflow = TextOverflow.Ellipsis,
                    )
                } else {
                    Spacer(modifier = Modifier.weight(1f))
                }

                CommentLikeItem(
                    modifier = Modifier.fillMaxWidth(),
                    post = data.post,
                )
            }
        }

        AnimatedVisibility(isTarget || isDownloaded) {
            var progress by rememberSaveable { mutableStateOf(0f) }
            val progressAnimation by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(300),
            )

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progressAnimation },
            )

            LaunchedEffect(isDownloaded) {
                if (isDownloaded) {
                    progress = 1f
                } else {
                    for (i in 0..20) {
                        progress = (i / 20f).coerceIn(0f..1f)
                        delay(Random.nextLong(10, 250))
                    }
                }
            }
        }
    }
}

@Composable
private fun CommentLikeItem(
    post: FanboxPost,
    modifier: Modifier = Modifier,
) {
    val likeColor = if (post.isLiked) Color(0xffe0405e) else MaterialTheme.colorScheme.onSurfaceVariant

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
                modifier = Modifier.size(16.dp),
                imageVector = Icons.Filled.Comment,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                contentDescription = null,
            )

            Text(
                text = post.commentCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
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
                text = post.likeCount.toString(),
                style = MaterialTheme.typography.bodySmall,
                color = likeColor,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Card(
            shape = RoundedCornerShape(4.dp),
            colors = CardDefaults.cardColors(MaterialTheme.colorScheme.secondaryContainer),
        ) {
            Text(
                modifier = Modifier.padding(6.dp, 4.dp),
                text = if (post.feeRequired == 0) {
                    stringResource(Res.string.fanbox_free_fee)
                } else {
                    stringResource(
                        Res.string.unit_jpy,
                        post.feeRequired,
                    )
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun CoverThumbnail(
    url: String?,
    isDownloaded: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        SubcomposeAsyncImage(
            modifier = Modifier.fillMaxSize(),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .fanboxHeader()
                .data(url)
                .build(),
            onError = {
                Napier.e("error: ${it.result.throwable}")
            },
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )

        AnimatedVisibility(
            modifier = Modifier.fillMaxSize(),
            visible = isDownloaded,
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.4f)),
            ) {
                Icon(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(48.dp),
                    imageVector = Icons.Filled.Check,
                    tint = Color.White,
                    contentDescription = null,
                )
            }
        }
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
