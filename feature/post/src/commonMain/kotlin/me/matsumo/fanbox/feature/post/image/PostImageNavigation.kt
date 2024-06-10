package me.matsumo.fanbox.feature.post.image

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val PostImageId = "postImageId"
const val PostImageIndex = "postImageIndex"
const val PostImageRoute = "postImage/{$PostImageId}/{$PostImageIndex}"

fun NavController.navigateToPostImage(postId: PostId, index: Int) {
    this.navigateWithLog("postImage/$postId/$index")
}

fun NavGraphBuilder.postImageScreen(
    terminate: () -> Unit,
) {
    composable(
        route = PostImageRoute,
        arguments = listOf(
            navArgument(PostImageId) { type = NavType.StringType },
            navArgument(PostImageIndex) { type = NavType.IntType },
        )
    ) {
        PostImageRoute(
            modifier = Modifier.fillMaxSize(),
            postId = PostId(it.arguments?.getString(PostImageId).orEmpty()),
            postImageIndex = it.arguments?.getInt(PostImageIndex) ?: 0,
            terminate = terminate,
        )
    }
}
