package me.matsumo.fanbox.feature.post.image

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

const val PostImageId = "postImageId"
const val PostImageIndex = "postImageIndex"
const val PostImageRoute = "postImage/{$PostImageId}/{$PostImageIndex}"

fun NavController.navigateToPostImage(postId: FanboxPostId, index: Int) {
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
        ),
    ) {
        PostImageRoute(
            modifier = Modifier.fillMaxSize(),
            postId = FanboxPostId(it.arguments?.getString(PostImageId).orEmpty()),
            postImageIndex = it.arguments?.getInt(PostImageIndex) ?: 0,
            terminate = terminate,
        )
    }
}
