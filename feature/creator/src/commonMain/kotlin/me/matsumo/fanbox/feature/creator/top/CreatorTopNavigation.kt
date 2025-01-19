package me.matsumo.fanbox.feature.creator.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavType
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import androidx.navigation.navDeepLink
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

const val CreatorTopId = "creatorTopId"
const val CreatorTopIsPosts = "creatorTopIsPosts"
const val CreatorTopRoute = "creatorTop/{$CreatorTopId}/{$CreatorTopIsPosts}"

fun NavController.navigateToCreatorTop(creatorId: FanboxCreatorId, isPosts: Boolean = false) {
    this.navigateWithLog("creatorTop/$creatorId/$isPosts")
}

fun NavGraphBuilder.creatorTopScreen(
    navigateToPostDetail: (FanboxPostId) -> Unit,
    navigateToPostSearch: (String, FanboxCreatorId) -> Unit,
    navigateToBillingPlus: (String?) -> Unit,
    navigateToPostByCreatorSearch: (FanboxCreatorId) -> Unit,
    navigateToDownloadAll: (FanboxCreatorId) -> Unit,
    navigateToAlertDialog: (SimpleAlertContents, () -> Unit, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    composable(
        route = CreatorTopRoute,
        arguments = listOf(
            navArgument(CreatorTopId) { type = NavType.StringType },
            navArgument(CreatorTopIsPosts) {
                type = NavType.BoolType
                defaultValue = true
            },
        ),
        deepLinks = listOf(
            navDeepLink { uriPattern = "https://www.fanbox.cc/@{$CreatorTopId}" },
            navDeepLink { uriPattern = "https://{$CreatorTopId}.fanbox.cc/" },
            navDeepLink { uriPattern = "https://{$CreatorTopId}.fanbox.cc" },
        )
    ) {
        CreatorTopRoute(
            modifier = Modifier.fillMaxSize(),
            creatorId = FanboxCreatorId(it.arguments?.getString(CreatorTopId).orEmpty()),
            isPosts = it.arguments?.getBoolean(CreatorTopIsPosts) ?: false,
            navigateToPostDetail = navigateToPostDetail,
            navigateToPostSearch = navigateToPostSearch,
            navigateToPostByCreatorSearch = navigateToPostByCreatorSearch,
            navigateToBillingPlus = navigateToBillingPlus,
            navigateToDownloadAll = navigateToDownloadAll,
            navigateToAlertDialog = navigateToAlertDialog,
            terminate = terminate,
        )
    }
}
