package me.matsumo.fanbox.feature.post.queue.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
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
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.model.FanboxDownloadItems
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.queue_item_post
import me.matsumo.fanbox.core.resources.queue_saving
import me.matsumo.fanbox.core.resources.unit_tag
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.theme.bold
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun DownloadQueueItem(
    items: FanboxDownloadItems,
    onCancelClicked: (String) -> Unit,
    modifier: Modifier = Modifier,
    progress: Float = -1f,
) {
    val thumbnailItems: List<@Composable () -> Unit> = items.items.map {
        when (it.type) {
            FanboxDownloadItems.Item.Type.Image -> {
                {
                    CoverThumbnail(
                        modifier = Modifier.fillMaxWidth(),
                        url = it.thumbnailUrl,
                    )
                }
            }

            FanboxDownloadItems.Item.Type.File -> {
                {
                    FileThumbnail(
                        modifier = Modifier.fillMaxWidth(),
                    )
                }
            }
        }
    }

    val actualThumbnailItems: List<@Composable () -> Unit> = when (val type = items.requestType) {
        FanboxDownloadItems.RequestType.File -> thumbnailItems
        FanboxDownloadItems.RequestType.Image -> thumbnailItems
        is FanboxDownloadItems.RequestType.Post -> {
            listOf(
                {
                    CoverThumbnail(
                        modifier = Modifier.fillMaxWidth(),
                        url = type.post?.cover?.url,
                    )
                },
            )
        }
    }

    Card(
        modifier = modifier,
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        Row(
            modifier = Modifier
                .height(80.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Grid(
                modifier = Modifier
                    .fillMaxHeight()
                    .aspectRatio(1f),
                items = actualThumbnailItems.toImmutableList(),
            )

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = items.title,
                    style = MaterialTheme.typography.titleMedium.bold(),
                    color = MaterialTheme.colorScheme.onSurface,
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 2,
                )

                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = when {
                        progress == 1f -> {
                            stringResource(Res.string.queue_saving)
                        }

                        else -> {
                            if (items.requestType is FanboxDownloadItems.RequestType.Post) {
                                stringResource(Res.string.queue_item_post)
                            } else {
                                stringResource(Res.string.unit_tag, items.items.size)
                            } + if (progress != -1f) {
                                " - (%.2f %%)".format(progress * 100)
                            } else {
                                ""
                            }
                        }
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    overflow = TextOverflow.Ellipsis,
                )
            }

            if (progress == -1f) {
                IconButton(onClick = { onCancelClicked.invoke(items.key) }) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = null,
                    )
                }
            }
        }

        if (progress >= 0f) {
            val progressAnimation by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(100),
            )

            if (progress == 0f || progress == 1f) {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                )
            } else {
                LinearProgressIndicator(
                    modifier = Modifier.fillMaxWidth(),
                    progress = { progressAnimation },
                )
            }
        }
    }
}

@Composable
private fun CoverThumbnail(
    url: String?,
    modifier: Modifier = Modifier,
) {
    SubcomposeAsyncImage(
        modifier = modifier,
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
private fun Grid(
    items: ImmutableList<@Composable () -> Unit>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(1.dp),
    ) {
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(1.dp),
        ) {
            items.getOrNull(0)?.let {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    content = { it() },
                )
            }

            items.getOrNull(1)?.let {
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    content = { it() },
                )
            }
        }

        if (items.size > 2) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(1.dp),
            ) {
                items.getOrNull(2)?.let {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        content = { it() },
                    )
                }

                items.getOrNull(3)?.let {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        content = { it() },
                    )
                }
            }
        }
    }
}
