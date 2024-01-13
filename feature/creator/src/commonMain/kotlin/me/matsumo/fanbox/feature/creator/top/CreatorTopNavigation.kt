package me.matsumo.fanbox.feature.creator.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder
import moe.tlaster.precompose.navigation.path

const val CreatorTopId = "creatorTopId"
const val CreatorTopIsPosts = "creatorTopIsPosts"
const val CreatorTopRoute = "creatorTop/{$CreatorTopId}/{$CreatorTopIsPosts}"

fun Navigator.navigateToCreatorTop(creatorId: CreatorId, isPosts: Boolean = false) {
    this.navigate("creatorTop/$creatorId/$isPosts")
}

fun RouteBuilder.creatorTopScreen(
    navigateToPostDetail: (PostId) -> Unit,
    navigateToPostSearch: (String, CreatorId) -> Unit,
    navigateToBillingPlus: () -> Unit,
    navigateToDownloadAll: (CreatorId) -> Unit,
    terminate: () -> Unit,
) {
    scene(
        route = CreatorTopRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        CreatorTopRoute(
            modifier = Modifier.fillMaxSize(),
            creatorId = CreatorId(it.path<String>(CreatorTopId).orEmpty()),
            isPosts = it.path<Boolean>(CreatorTopIsPosts) ?: false,
            navigateToPostDetail = navigateToPostDetail,
            navigateToPostSearch = navigateToPostSearch,
            navigateToBillingPlus = navigateToBillingPlus,
            navigateToDownloadAll = navigateToDownloadAll,
            terminate = terminate,
        )
    }
}
