package me.matsumo.fanbox.feature.post.queue

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

fun NavGraphBuilder.downloadQueueScreen(
    navigateToPostDetail: (FanboxPostId) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.DownloadQueue> {
        DownloadQueueScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToPostDetail = navigateToPostDetail,
            terminate = terminate,
        )
    }
}
