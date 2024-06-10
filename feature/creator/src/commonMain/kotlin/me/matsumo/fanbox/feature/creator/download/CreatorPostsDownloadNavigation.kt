package me.matsumo.fanbox.feature.creator.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument

const val CreatorPostsDownloadId = "creatorPostsDownloadId"
const val CreatorPostsDownloadRoute = "creatorPostsDownload/{$CreatorPostsDownloadId}"

fun NavController.navigateToCreatorPostsDownload(creatorId: CreatorId) {
    this.navigate("creatorPostsDownload/$creatorId")
}

fun NavGraphBuilder.creatorPostsDownloadDialog(
    navigateToCancelDownloadAlert: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    composable(
        route = CreatorPostsDownloadRoute,
        arguments = listOf(navArgument(CreatorPostsDownloadId) { type = NavType.StringType })
    ) {
        CreatorPostsDownloadRoute(
            modifier = Modifier.fillMaxSize(),
            creatorId = CreatorId(it.arguments?.getString(CreatorPostsDownloadId).orEmpty()),
            navigateToCancelDownloadAlert = navigateToCancelDownloadAlert,
            terminate = terminate,
        )
    }
}
