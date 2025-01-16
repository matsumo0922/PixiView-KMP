package me.matsumo.fanbox.core.ui.component.video

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.UIKitView
import kotlinx.coroutines.delay
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxSessionId
import platform.AVFoundation.AVPlayer
import platform.AVFoundation.AVPlayerItem
import platform.AVFoundation.AVPlayerLayer
import platform.AVFoundation.AVURLAsset
import platform.AVFoundation.pause
import platform.AVFoundation.play
import platform.AVKit.AVPlayerViewController
import platform.Foundation.NSURL

@Composable
actual fun VideoPlayer(url: String, modifier: Modifier) {
    val sessionId = LocalFanboxSessionId.current.value

    val player = remember { getAVPlayerItem(url, sessionId)?.let { AVPlayer(it) } }
    val playerLayer = remember { AVPlayerLayer() }
    val avPlayerViewController = remember { AVPlayerViewController() }

    var isDisplayController by remember { mutableStateOf(false) }
    var isPlaying by remember { mutableStateOf(true) }

    SideEffect {
        playerLayer.player = player
        avPlayerViewController.player = player
        avPlayerViewController.showsPlaybackControls = false
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
    ) {
        UIKitView(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            factory = { avPlayerViewController.view },
            update = { player?.play() },
        )

        AnimatedVisibility(
            modifier = Modifier
                .fillMaxWidth()
                .aspectRatio(16f / 9f),
            visible = isDisplayController,
            enter = fadeIn(),
            exit = fadeOut(),
        ) {
            ControllerView(
                modifier = Modifier.fillMaxSize(),
                isPlaying = isPlaying,
                onPlayPauseClicked = {
                    isPlaying = it

                    if (it) {
                        player?.play()
                    } else {
                        player?.pause()
                    }
                },
            )
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
                    tint = Color.White,
                )
            } else {
                Icon(
                    modifier = Modifier.size(48.dp),
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                )
            }
        }
    }
}

private fun getAVPlayerItem(url: String, sessionId: String): AVPlayerItem? {
    val nsUrl = NSURL.URLWithString(url) ?: return null
    val headers = mutableMapOf<String, String>().apply {
        put("origin", "https://www.fanbox.cc")
        put("referer", "https://www.fanbox.cc")
        put("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/98.0.4758.102 Safari/537.36")

        if (sessionId.isNotBlank()) {
            set("Cookie", "FANBOXSESSID=$sessionId")
        }
    }
    val asset = AVURLAsset.URLAssetWithURL(nsUrl, headers.toMap())

    return AVPlayerItem(asset)
}
