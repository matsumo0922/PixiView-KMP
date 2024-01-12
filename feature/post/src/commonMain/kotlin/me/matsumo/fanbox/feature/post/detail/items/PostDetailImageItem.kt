package me.matsumo.fanbox.feature.post.detail.items

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.ui.extensition.SimmerPlaceHolder
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.feature.post.image.items.PostImageMenuDialog

@OptIn(ExperimentalFoundationApi::class)
@Composable
internal fun PostDetailImageItem(
    item: FanboxPostDetail.ImageItem,
    onClickImage: (FanboxPostDetail.ImageItem) -> Unit,
    onClickDownload: () -> Unit,
    onClickAllDownload: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isShowMenu by remember { mutableStateOf(false) }
    val loadUrl = if (item.extension.lowercase() == "gif") item.originalUrl else item.thumbnailUrl

    SubcomposeAsyncImage(
        modifier = modifier
            .aspectRatio(item.aspectRatio)
            .combinedClickable(
                onClick = { onClickImage.invoke(item) },
                onLongClick = { isShowMenu = true },
            ),
        model = ImageRequest.Builder(LocalPlatformContext.current)
            .fanboxHeader()
            .data(loadUrl)
            .build(),
        loading = {
            SimmerPlaceHolder()
        },
        contentDescription = null,
    )

    if (isShowMenu) {
        PostImageMenuDialog(
            onClickDownload = { onClickDownload.invoke() },
            onClickAllDownload = { onClickAllDownload.invoke() },
            onDismissRequest = { isShowMenu = false },
        )
    }
}
