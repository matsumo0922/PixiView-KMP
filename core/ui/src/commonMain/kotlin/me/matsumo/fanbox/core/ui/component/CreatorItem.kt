package me.matsumo.fanbox.core.ui.component

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_follow
import me.matsumo.fanbox.core.resources.common_supporting
import me.matsumo.fanbox.core.resources.common_unfollow
import me.matsumo.fanbox.core.resources.im_default_user
import me.matsumo.fanbox.core.ui.extensition.FadePlaceHolder
import me.matsumo.fanbox.core.ui.extensition.SimmerPlaceHolder
import me.matsumo.fanbox.core.ui.extensition.asCoilImage
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.jetbrains.compose.resources.stringResource

@Composable
fun CreatorItem(
    creatorDetail: FanboxCreatorDetail,
    onClickCreator: (FanboxCreatorId) -> Unit,
    onClickFollow: (FanboxUserId) -> Unit,
    onClickUnfollow: (FanboxUserId) -> Unit,
    onClickSupporting: (String) -> Unit,
    modifier: Modifier = Modifier,
    isFollowed: Boolean = creatorDetail.isFollowed,
    isShowCoverImage: Boolean = true,
    isShowDescription: Boolean = true,
) {
    var isEllipsized by rememberSaveable { mutableStateOf(false) }
    var isDisplayedAll by rememberSaveable { mutableStateOf(false) }

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClickCreator.invoke(creatorDetail.creatorId) },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(MaterialTheme.colorScheme.surfaceColorAtElevation(2.dp)),
    ) {
        if (isShowCoverImage) {
            if (creatorDetail.coverImageUrl != null) {
                SubcomposeAsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .clip(RoundedCornerShape(4.dp)),
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .fanboxHeader()
                        .data(creatorDetail.coverImageUrl)
                        .build(),
                    loading = {
                        SimmerPlaceHolder()
                    },
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            } else if (creatorDetail.profileItems.isNotEmpty()) {
                HorizontalPager(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16 / 9f)
                        .clip(RoundedCornerShape(4.dp)),
                    state = rememberPagerState { creatorDetail.profileItems.size },
                ) {
                    SubcomposeAsyncImage(
                        modifier = Modifier.fillMaxWidth(),
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .fanboxHeader()
                            .data(creatorDetail.profileItems[it].thumbnailUrl)
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

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            UserSection(
                modifier = Modifier.fillMaxWidth(),
                creatorDetail = creatorDetail,
                isFollowed = isFollowed,
                onClickFollow = onClickFollow,
                onClickUnfollow = onClickUnfollow,
                onClickSupporting = onClickSupporting,
            )

            if (creatorDetail.description.isNotBlank() && isShowDescription) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    Text(
                        modifier = Modifier
                            .weight(1f)
                            .animateContentSize(),
                        text = creatorDetail.description.trimEnd(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = if (isDisplayedAll) Int.MAX_VALUE else 3,
                        overflow = TextOverflow.Ellipsis,
                        onTextLayout = {
                            isEllipsized = it.hasVisualOverflow
                        },
                    )

                    if (isEllipsized && !isDisplayedAll) {
                        IconButton(onClick = { isDisplayedAll = true }) {
                            Icon(
                                imageVector = Icons.Default.KeyboardArrowDown,
                                contentDescription = null,
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun UserSection(
    creatorDetail: FanboxCreatorDetail,
    isFollowed: Boolean,
    onClickFollow: (FanboxUserId) -> Unit,
    onClickUnfollow: (FanboxUserId) -> Unit,
    onClickSupporting: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .error(Res.drawable.im_default_user.asCoilImage())
                .data(creatorDetail.user?.iconUrl)
                .build(),
            contentDescription = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = creatorDetail.user?.name.orEmpty(),
                style = MaterialTheme.typography.titleMedium.bold(),
                color = MaterialTheme.colorScheme.primary,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "@${creatorDetail.user?.creatorId ?: "Unknown"}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            creatorDetail.isSupported -> {
                Button(onClick = { onClickSupporting.invoke(creatorDetail.supportingBrowserUrl) }) {
                    Text(stringResource(Res.string.common_supporting))
                }
            }

            isFollowed -> {
                OutlinedButton(onClick = { creatorDetail.user?.userId?.let(onClickUnfollow) }) {
                    Text(stringResource(Res.string.common_unfollow))
                }
            }

            else -> {
                Button(onClick = { creatorDetail.user?.userId?.let(onClickFollow) }) {
                    Text(stringResource(Res.string.common_follow))
                }
            }
        }
    }
}
