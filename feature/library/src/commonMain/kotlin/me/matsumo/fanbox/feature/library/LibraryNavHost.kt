package me.matsumo.fanbox.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import caios.android.fanbox.feature.library.home.LibraryHomeRoute
import caios.android.fanbox.feature.library.home.libraryHomeScreen
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.library.discovery.LibraryDiscoveryRoute
import me.matsumo.fanbox.feature.library.discovery.libraryDiscoveryScreen
import me.matsumo.fanbox.feature.library.message.LibraryMessageRoute
import me.matsumo.fanbox.feature.library.message.libraryMessageScreen
import me.matsumo.fanbox.feature.library.notify.LibraryNotifyRoute
import me.matsumo.fanbox.feature.library.notify.libraryNotifyScreen
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

@Composable
fun LibraryNavHost(
    navController: Navigator,
    openDrawer: () -> Unit,
    navigateToPostSearch: () -> Unit,
    navigateToPostDetailFromHome: (postId: PostId) -> Unit,
    navigateToPostDetailFromSupported: (postId: PostId) -> Unit,
    navigateToCreatorPosts: (creatorId: CreatorId) -> Unit,
    navigateToCreatorPlans: (creatorId: CreatorId) -> Unit,
    navigateToCancelPlus: (SimpleAlertContents) -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryHomeRoute,
    applyOtherRoutes: RouteBuilder.() -> Unit = {},
) {
    NavHost(
        modifier = modifier,
        navigator = navController,
        initialRoute = startDestination,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        libraryHomeScreen(
            openDrawer = openDrawer,
            navigateToPostDetailFromHome = navigateToPostDetailFromHome,
            navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToCancelPlus = navigateToCancelPlus,
        )

        libraryDiscoveryScreen(
            openDrawer = openDrawer,
            navigateToPostSearch = navigateToPostSearch,
            navigateToCreatorPosts = navigateToCreatorPosts,
        )

        libraryNotifyScreen(
            openDrawer = openDrawer,
            navigateToPostDetail = navigateToPostDetailFromHome,
        )

        libraryMessageScreen(
            openDrawer = openDrawer,
            navigateToCreatorPosts = navigateToCreatorPosts,
        )

        applyOtherRoutes.invoke(this)
    }
}

internal val LibraryRoutes = listOf(
    LibraryHomeRoute,
    LibraryDiscoveryRoute,
    LibraryNotifyRoute,
    LibraryMessageRoute,
)
