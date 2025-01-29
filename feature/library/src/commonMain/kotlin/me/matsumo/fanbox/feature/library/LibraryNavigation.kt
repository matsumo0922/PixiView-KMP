package me.matsumo.fanbox.feature.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

fun NavGraphBuilder.libraryScreen(
    navHostController: NavHostController,
    navigateToPostSearch: () -> Unit,
    navigateToPostByCreatorSearch: (FanboxCreatorId) -> Unit,
    navigateToPostDetailFromHome: (postId: FanboxPostId) -> Unit,
    navigateToPostDetailFromSupported: (postId: FanboxPostId) -> Unit,
    navigateToCreatorPosts: (creatorId: FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (creatorId: FanboxCreatorId) -> Unit,
    navigateToBookmarkedPosts: () -> Unit,
    navigateToFollowerCreators: () -> Unit,
    navigateToSupportingCreators: () -> Unit,
    navigateToPayments: () -> Unit,
    navigateToDownloadQueue: () -> Unit,
    navigateToSettingTop: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToBillingPlus: (String?) -> Unit,
    navigateToCancelPlus: (SimpleAlertContents) -> Unit,
) {
    composable<Destination.Library> {
        LibraryScreen(
            modifier = Modifier.fillMaxSize(),
            navHostController = navHostController,
            navigateToPostSearch = navigateToPostSearch,
            navigateToPostByCreatorSearch = navigateToPostByCreatorSearch,
            navigateToPostDetailFromHome = navigateToPostDetailFromHome,
            navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToFollowerCreators = navigateToFollowerCreators,
            navigateToSupportingCreators = navigateToSupportingCreators,
            navigateToBookmarkedPosts = navigateToBookmarkedPosts,
            navigateToPayments = navigateToPayments,
            navigateToDownloadQueue = navigateToDownloadQueue,
            navigateToSettingTop = navigateToSettingTop,
            navigateToAbout = navigateToAbout,
            navigateToBillingPlus = navigateToBillingPlus,
            navigateToCancelPlus = navigateToCancelPlus,
        )
    }
}
