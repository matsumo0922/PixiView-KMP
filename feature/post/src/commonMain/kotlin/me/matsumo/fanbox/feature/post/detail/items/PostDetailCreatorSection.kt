package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.AsyncImage
import coil3.compose.LocalPlatformContext
import coil3.request.ImageRequest
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.extensition.asCoilImage
import me.matsumo.fanbox.core.ui.theme.bold

@Composable
internal fun PostDetailCreatorSection(
    postDetail: FanboxPostDetail,
    creatorDetail: FanboxCreatorDetail,
    onClickCreator: (CreatorId) -> Unit,
    onClickFollow: (String) -> Unit,
    onClickUnfollow: (String) -> Unit,
    onClickSupporting: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            modifier = Modifier
                .padding(
                    top = 16.dp,
                    start = 16.dp,
                    end = 16.dp,
                )
                .fillMaxWidth(),
            text = stringResource(MR.strings.post_detail_creator),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        CreatorItem(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClickCreator.invoke(postDetail.user.creatorId) }
                .padding(
                    top = 16.dp,
                    start = 24.dp,
                    end = 24.dp,
                    bottom = 16.dp,
                ),
            postDetail = postDetail,
            creatorDetail = creatorDetail,
            onClickFollow = onClickFollow,
            onClickUnfollow = onClickUnfollow,
            onClickSupporting = onClickSupporting,
        )
    }
}

@OptIn(ExperimentalCoilApi::class)
@Composable
private fun CreatorItem(
    postDetail: FanboxPostDetail,
    creatorDetail: FanboxCreatorDetail,
    onClickFollow: (String) -> Unit,
    onClickUnfollow: (String) -> Unit,
    onClickSupporting: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    var isFollowed by remember(creatorDetail.isFollowed) { mutableStateOf(creatorDetail.isFollowed) }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        AsyncImage(
            modifier = Modifier
                .clip(CircleShape)
                .size(36.dp),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .error(MR.images.im_default_user.asCoilImage())
                .data(postDetail.user.iconUrl)
                .build(),
            contentDescription = null,
        )

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = postDetail.user.name,
                style = MaterialTheme.typography.bodyLarge.bold(),
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = "@${postDetail.user.creatorId.value}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        when {
            creatorDetail.isSupported -> {
                Button(onClick = { onClickSupporting.invoke(creatorDetail.supportingBrowserUrl) }) {
                    Text(stringResource(MR.strings.common_supporting))
                }
            }

            creatorDetail.isFollowed -> {
                OutlinedButton(
                    onClick = {
                        isFollowed = false
                        onClickUnfollow.invoke(creatorDetail.user.userId)
                    },
                ) {
                    Text(stringResource(MR.strings.common_unfollow))
                }
            }

            else -> {
                Button(
                    onClick = {
                        isFollowed = true
                        onClickFollow.invoke(creatorDetail.user.userId)
                    },
                ) {
                    Text(stringResource(MR.strings.common_follow))
                }
            }
        }
    }
}
