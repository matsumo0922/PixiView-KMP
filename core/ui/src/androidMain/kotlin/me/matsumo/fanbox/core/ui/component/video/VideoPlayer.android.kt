package me.matsumo.fanbox.core.ui.component.video

import android.os.Build
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import kotlinx.coroutines.delay
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxCookie
import me.matsumo.fanbox.core.ui.view.LoadingView

@OptIn(UnstableApi::class)
@Composable
actual fun VideoPlayer(url: String, modifier: Modifier) {
    val context = LocalContext.current
    val cookie = LocalFanboxCookie.current.cookie

    var playerSize by remember { mutableStateOf(IntSize.Zero) }
    var isDisplayController by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var isPlaying by remember { mutableStateOf(true) }

    val exoPlayer = remember {
        ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true)
            .setMediaSourceFactory(getMediaSourceFactory(cookie))
            .build()
    }

    DisposableEffect(exoPlayer) {
        val mediaItem = MediaItem.fromUri(url)

        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        exoPlayer.seekTo(0)
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(playbackState: Int) {
                super.onPlaybackStateChanged(playbackState)
                isLoading = playbackState == Player.STATE_BUFFERING
            }
        })

        exoPlayer.repeatMode = Player.REPEAT_MODE_ONE
        exoPlayer.playWhenReady = true

        onDispose {
            exoPlayer.pause()
            exoPlayer.release()
        }
    }

    LaunchedEffect(isDisplayController) {
        if (isDisplayController) {
            delay(2000)
            isDisplayController = false
        }
    }

    Box(
        modifier = modifier.pointerInput(true) {
            detectTapGestures {
                isDisplayController = !isDisplayController
            }
        },
        contentAlignment = Alignment.Center,
    ) {
        AndroidView(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { playerSize = it.size },
            factory = {
                PlayerView(context).apply {
                    player = exoPlayer
                    resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIXED_WIDTH
                    useController = false

                    if (Build.VERSION.SDK_INT >= 29) {
                        transitionAlpha = 0.5f
                    }
                }
            },
        )

        if (isLoading) {
            LoadingView(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9),
            )
        } else {
            AnimatedVisibility(
                modifier = Modifier.size(playerSize.toDpSize()),
                visible = isDisplayController,
                enter = fadeIn(),
                exit = fadeOut(),
            ) {
                ControllerView(
                    modifier = Modifier.fillMaxSize(),
                    isPlaying = isPlaying,
                    onPlayPauseClicked = {
                        isPlaying = it
                        exoPlayer.playWhenReady = it
                    },
                )
            }
        }
    }
}

@Composable
private fun ControllerView(
    isPlaying: Boolean,
    onPlayPauseClicked: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier.background(Color.Black.copy(alpha = 0.4f))) {
        IconButton(
            modifier = Modifier.align(Alignment.Center),
            onClick = { onPlayPauseClicked(!isPlaying) },
        ) {
            if (isPlaying) {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Default.Pause,
                    contentDescription = null,
                )
            } else {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                )
            }
        }
    }
}

@Composable
private fun IntSize.toDpSize(): DpSize {
    return with(LocalDensity.current) { DpSize(width.toDp(), height.toDp()) }
}

@OptIn(UnstableApi::class)
private fun getMediaSourceFactory(cookie: String): MediaSource.Factory {
    val userAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36"
    val headers = mutableMapOf<String, String>().apply {
        put("origin", "https://www.fanbox.cc")
        put("referer", "https://www.fanbox.cc")
        put("user-agent", userAgent)

        if (cookie.isNotBlank()) {
            set("Cookie", cookie)
        }
    }

    val dataSourceFactory = DefaultHttpDataSource.Factory()
        .setUserAgent(userAgent)
        .setDefaultRequestProperties(headers)

    return DefaultMediaSourceFactory(dataSourceFactory)
}
