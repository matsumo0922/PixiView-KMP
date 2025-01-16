package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.InsertDriveFile
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.FanboxPostId
import me.matsumo.fanbox.core.ui.extensition.SimmerPlaceHolder
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader

@Composable
fun PostGridItem(
    post: FanboxPost,
    onClickPost: (FanboxPostId) -> Unit,
    isHideAdultContents: Boolean,
    isOverrideAdultContents: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier.aspectRatio(1f)) {
        when {
            post.isRestricted -> {
                RestrictThumbnail(
                    modifier = Modifier.fillMaxSize(),
                )
            }

            post.cover == null -> {
                FileThumbnail(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClickPost.invoke(post.id) },
                )
            }

            post.hasAdultContent && (isHideAdultContents || !isOverrideAdultContents) -> {
                AdultContentGridThumbnail(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClickPost.invoke(post.id) },
                    coverImageUrl = post.cover?.url,
                    isAllowedShow = isOverrideAdultContents,
                )
            }

            else -> {
                SubcomposeAsyncImage(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClickPost.invoke(post.id) },
                    model = ImageRequest.Builder(LocalPlatformContext.current)
                        .fanboxHeader()
                        .data(post.cover?.url)
                        .build(),
                    loading = {
                        SimmerPlaceHolder()
                    },
                    contentScale = ContentScale.Crop,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun RestrictThumbnail(
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
            modifier = Modifier.size(40.dp),
            imageVector = Icons.Default.Lock,
            tint = Color.White,
            contentDescription = null,
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
            modifier = Modifier.size(40.dp),
            imageVector = Icons.Filled.InsertDriveFile,
            tint = Color.White,
            contentDescription = null,
        )
    }
}

@Composable
private fun AdultContentGridThumbnail(
    coverImageUrl: String?,
    modifier: Modifier = Modifier,
    isAllowedShow: Boolean = false,
) {
    Box(modifier) {
        SubcomposeAsyncImage(
            modifier = Modifier
                .fillMaxSize()
                .blur(20.dp),
            model = ImageRequest.Builder(LocalPlatformContext.current)
                .fanboxHeader()
                .data(coverImageUrl)
                .build(),
            loading = {
                SimmerPlaceHolder()
            },
            contentScale = ContentScale.Crop,
            contentDescription = null,
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(if (isAllowedShow) Color.Black.copy(alpha = 0.5f) else Color.DarkGray),
        )
    }
}
