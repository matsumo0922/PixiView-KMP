package me.matsumo.fanbox.feature.post.queue

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

const val DownloadQueueRoute = "downloadQueue"

fun NavController.navigateToDownloadQueue() {
    this.navigateWithLog(DownloadQueueRoute)
}

fun NavGraphBuilder.downloadQueueScreen(
    navigateToPostDetail: (FanboxPostId) -> Unit,
    terminate: () -> Unit,
) {
    composable(DownloadQueueRoute) {
        DownloadQueueScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToPostDetail = navigateToPostDetail,
            terminate = terminate,
        )
    }
}
