package me.matsumo.fanbox.feature.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val LibraryRoute = "library"

fun Navigator.navigateToLibrary() {
    this.navigate(LibraryRoute)
}

fun RouteBuilder.libraryScreen(
    navigateToPostSearch: () -> Unit,
    navigateToPostDetailFromHome: (postId: PostId) -> Unit,
    navigateToPostDetailFromSupported: (postId: PostId) -> Unit,
    navigateToCreatorPosts: (creatorId: CreatorId) -> Unit,
    navigateToCreatorPlans: (creatorId: CreatorId) -> Unit,
    navigateToBookmarkedPosts: () -> Unit,
    navigateToFollowerCreators: () -> Unit,
    navigateToSupportingCreators: () -> Unit,
    navigateToPayments: () -> Unit,
    navigateToSettingTop: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToBillingPlus: () -> Unit,
    navigateToCancelPlus: (SimpleAlertContents) -> Unit,
) {
    scene(
        route = LibraryRoute,
    ) {
        LibraryScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToPostSearch = navigateToPostSearch,
            navigateToPostDetailFromHome = navigateToPostDetailFromHome,
            navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToFollowerCreators = navigateToFollowerCreators,
            navigateToSupportingCreators = navigateToSupportingCreators,
            navigateToBookmarkedPosts = navigateToBookmarkedPosts,
            navigateToPayments = navigateToPayments,
            navigateToSettingTop = navigateToSettingTop,
            navigateToAbout = navigateToAbout,
            navigateToBillingPlus = navigateToBillingPlus,
            navigateToCancelPlus = navigateToCancelPlus,
        )
    }
}
