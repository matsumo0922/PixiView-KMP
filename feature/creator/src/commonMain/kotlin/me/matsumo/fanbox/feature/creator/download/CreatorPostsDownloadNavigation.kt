package me.matsumo.fanbox.feature.creator.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

const val CreatorPostsDownloadId = "creatorPostsDownloadId"
const val CreatorPostsDownloadRoute = "creatorPostsDownload/{$CreatorPostsDownloadId}"

fun NavController.navigateToCreatorPostsDownload(creatorId: FanboxCreatorId) {
    this.navigateWithLog("creatorPostsDownload/$creatorId")
}

fun NavGraphBuilder.creatorPostsDownloadDialog(
    navigateToCancelDownloadAlert: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    composable(
        route = CreatorPostsDownloadRoute,
        arguments = listOf(navArgument(CreatorPostsDownloadId) { type = NavType.StringType }),
    ) {
        CreatorPostsDownloadRoute(
            modifier = Modifier.fillMaxSize(),
            creatorId = FanboxCreatorId(it.arguments?.getString(CreatorPostsDownloadId).orEmpty()),
            navigateToCancelDownloadAlert = navigateToCancelDownloadAlert,
            terminate = terminate,
        )
    }
}
