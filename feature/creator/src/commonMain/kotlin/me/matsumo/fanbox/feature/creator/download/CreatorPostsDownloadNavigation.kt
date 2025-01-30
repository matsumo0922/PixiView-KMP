package me.matsumo.fanbox.feature.creator.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.customNavTypes

fun NavGraphBuilder.creatorPostsDownloadDialog(
    navigateTo: (Destination) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.CreatorPostsDownload>(
        typeMap = customNavTypes,
    ) {
        CreatorPostsDownloadRoute(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
