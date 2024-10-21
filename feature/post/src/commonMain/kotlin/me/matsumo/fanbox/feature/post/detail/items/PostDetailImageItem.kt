package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import coil3.annotation.ExperimentalCoilApi
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import coil3.size.Size
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_ios_gif_support
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.SimmerPlaceHolder
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.theme.center
import me.matsumo.fanbox.feature.post.image.items.PostImageMenuDialog
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalFoundationApi::class, ExperimentalCoilApi::class)
@Composable
internal fun PostDetailImageItem(
    item: FanboxPostDetail.ImageItem,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickDownload: () -> Unit,
    onClickAllDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isShowMenu by remember { mutableStateOf(false) }
    var currentAspectRatio by remember { mutableStateOf(item.aspectRatio) }
    val loadUrl = if (item.extension.lowercase() == "gif") item.originalUrl else item.thumbnailUrl

    Box(modifier) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(currentAspectRatio)
                .combinedClickable(
                    onClick = { onClickImage.invoke(item) },
                    onLongClick = { isShowMenu = true },
                ),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .fanboxHeader()
                .data(loadUrl)
                .size(Size.ORIGINAL)
                .build(),
            loading = {
                SimmerPlaceHolder()
            },
            onSuccess = {
                currentAspectRatio = it.result.image.width.toFloat() / it.result.image.height
            },
            contentDescription = null,
        )

        if (item.extension.lowercase() == "gif" && currentPlatform == Platform.IOS) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .aspectRatio(currentAspectRatio)
                    .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
            ) {
                Text(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .padding(24.dp),
                    text = stringResource(Res.string.error_ios_gif_support),
                    style = MaterialTheme.typography.bodyLarge.center(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }

    PostImageMenuDialog(
        isVisible = isShowMenu,
        onClickDownload = { onClickDownload.invoke() },
        onClickAllDownload = { onClickAllDownload.invoke() },
        onDismissRequest = { isShowMenu = false },
    )
}
