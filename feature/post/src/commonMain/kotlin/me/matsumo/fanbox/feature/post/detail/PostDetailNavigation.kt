@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.feature.post.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import androidx.navigation.navDeepLink
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.core.ui.customNavTypes

fun NavGraphBuilder.postDetailScreen(
    navigateTo: (Destination) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.PostDetail>(
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://www.fanbox.cc/@{creatorId}/posts/{postId}" },
            navDeepLink { uriPattern = "https://{creatorId}.fanbox.cc/posts/{postId}" },
        ),
        typeMap = customNavTypes,
    ) {
        PostDetailRoute(
            modifier = Modifier.fillMaxSize(),
            postId = it.toRoute<Destination.PostDetail>().postId,
            navigateTo = navigateTo,
            navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
            terminate = terminate,
        )
    }
}
