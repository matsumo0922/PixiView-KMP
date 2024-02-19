package me.matsumo.fanbox.feature.creator.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path

const val CreatorPostsDownloadId = "creatorPostsDownloadId"
const val CreatorPostsDownloadRoute = "creatorPostsDownload/{$CreatorPostsDownloadId}"

fun Navigator.navigateToCreatorPostsDownload(creatorId: CreatorId) {
    this.navigate("creatorPostsDownload/$creatorId")
}

fun RouteBuilder.creatorPostsDownloadDialog(
    navigateToCancelDownloadAlert: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    scene(CreatorPostsDownloadRoute) {
        CreatorPostsDownloadRoute(
            modifier = Modifier.fillMaxSize(),
            creatorId = CreatorId(it.path<String>(CreatorPostsDownloadId).orEmpty()),
            navigateToCancelDownloadAlert = navigateToCancelDownloadAlert,
            terminate = terminate,
        )
    }
}
