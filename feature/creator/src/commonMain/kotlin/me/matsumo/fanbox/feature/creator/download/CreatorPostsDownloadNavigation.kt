package me.matsumo.fanbox.feature.creator.download

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path

const val CreatorPostsDownloadId = "creatorPostsDownloadId"
const val CreatorPostsDownloadRoute = "creatorPostsDownload/{$CreatorPostsDownloadId}"

fun Navigator.navigateToCreatorPostsDownload(creatorId: CreatorId) {
    this.navigate("creatorPostsDownload/$creatorId")
}

fun RouteBuilder.creatorPostsDownloadDialog(
    terminate: () -> Unit,
) {
    dialog(
        route = CreatorPostsDownloadRoute,
    ) {
        Dialog(
            onDismissRequest = terminate,
            properties = DialogProperties(
                dismissOnBackPress = false,
                dismissOnClickOutside = false,
            ),
        ) {
            CreatorPostsDownloadRoute(
                modifier = Modifier.fillMaxWidth(),
                creatorId = CreatorId(it.path<String>(CreatorPostsDownloadId).orEmpty()),
                terminate = terminate,
            )
        }
    }
}
