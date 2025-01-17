package me.matsumo.fanbox.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.surfaceColorAtElevation
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.PixiViewNavHost
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.post_detail_downloading
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation.decelerateEasing
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun PixiViewContent(
    downloadState: DownloadState,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        PixiViewNavHost(
            modifier = Modifier.fillMaxSize(),
        )
    }
}
