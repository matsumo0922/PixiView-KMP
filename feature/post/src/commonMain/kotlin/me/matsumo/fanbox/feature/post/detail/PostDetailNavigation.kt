@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.feature.post.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path

const val PostDetailId = "postDetailId"
const val PostDetailType = "postDetailPagingType"
const val PostDetailRoute = "postDetail/{$PostDetailId}/{$PostDetailType}"

fun Navigator.navigateToPostDetail(postId: PostId, pagingType: PostDetailPagingType) {
    this.navigate("postDetail/$postId/${pagingType.name}")
}

fun RouteBuilder.postDetailScreen(
    navigateToPostSearch: (String, CreatorId) -> Unit,
    navigateToPostDetail: (PostId) -> Unit,
    navigateToPostImage: (PostId, Int) -> Unit,
    navigateToCreatorPosts: (CreatorId) -> Unit,
    navigateToCreatorPlans: (CreatorId) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    scene(
        route = PostDetailRoute,
        navTransition = NavigateAnimation.Horizontal.transition
    ) {
        PostDetailRoute(
            modifier = Modifier.fillMaxSize(),
            postId = PostId(it.path<String>(PostDetailId).orEmpty()),
            type = PostDetailPagingType.valueOf(it.path<String>(PostDetailType) ?: PostDetailPagingType.Unknown.name),
            navigateToPostSearch = navigateToPostSearch,
            navigateToPostDetail = navigateToPostDetail,
            navigateToPostImage = navigateToPostImage,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
            terminate = terminate,
        )
    }
}

enum class PostDetailPagingType {
    Home,
    Supported,
    Creator,
    Search,
    Unknown,
}
