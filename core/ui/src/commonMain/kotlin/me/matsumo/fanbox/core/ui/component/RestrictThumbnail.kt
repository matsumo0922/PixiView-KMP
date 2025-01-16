package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_restricted_post
import me.matsumo.fanbox.core.resources.error_restricted_post_ios
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.SimmerPlaceHolder
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.theme.center
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun RestrictThumbnail(
    feeRequired: Int,
    coverImageUrl: String?,
    modifier: Modifier = Modifier,
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
                .background(Color.Black.copy(alpha = 0.5f)),
        )

        Column(
            modifier = Modifier
                .align(Alignment.Center)
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
                text = if (currentPlatform == Platform.Android) {
                    stringResource(
                        Res.string.error_restricted_post,
                        feeRequired,
                    )
                } else {
                    stringResource(Res.string.error_restricted_post_ios)
                },
                style = MaterialTheme.typography.bodySmall.center(),
                color = Color.White,
            )
        }
    }
}
