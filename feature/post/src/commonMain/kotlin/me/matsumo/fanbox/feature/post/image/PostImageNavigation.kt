package me.matsumo.fanbox.feature.post.image

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path

const val PostImageId = "postImageId"
const val PostImageIndex = "postImageIndex"
const val PostImageRoute = "postImage/{$PostImageId}/{$PostImageIndex}"

fun Navigator.navigateToPostImage(postId: PostId, index: Int) {
    this.navigate("postImage/$postId/$index")
}

fun RouteBuilder.postImageScreen(
    terminate: () -> Unit,
) {
    scene(
        route = PostImageRoute,
        navTransition = NavigateAnimation.Horizontal.transition
    ) {
        PostImageRoute(
            modifier = Modifier.fillMaxSize(),
            postId = PostId(it.path<String>(PostImageId).orEmpty()),
            postImageIndex = it.path<Int>(PostImageIndex) ?: 0,
            terminate = terminate,
        )
    }
}
