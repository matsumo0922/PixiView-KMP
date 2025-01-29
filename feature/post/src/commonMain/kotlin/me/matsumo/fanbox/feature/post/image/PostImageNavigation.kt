package me.matsumo.fanbox.feature.post.image

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.model.Destination

fun NavGraphBuilder.postImageScreen(
    navigateToDownloadQueue: () -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.PostImage> {
        PostImageRoute(
            modifier = Modifier.fillMaxSize(),
            postId = it.toRoute<Destination.PostImage>().postId,
            postImageIndex = it.toRoute<Destination.PostImage>().index,
            navigateToDownloadQueue = navigateToDownloadQueue,
            terminate = terminate,
        )
    }
}
