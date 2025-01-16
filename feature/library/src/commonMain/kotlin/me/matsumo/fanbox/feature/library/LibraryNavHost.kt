package me.matsumo.fanbox.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.library.discovery.LibraryDiscoveryRoute
import me.matsumo.fanbox.feature.library.discovery.libraryDiscoveryScreen
import me.matsumo.fanbox.feature.library.home.LibraryHomeRoute
import me.matsumo.fanbox.feature.library.home.libraryHomeScreen
import me.matsumo.fanbox.feature.library.message.LibraryMessageRoute
import me.matsumo.fanbox.feature.library.message.libraryMessageScreen
import me.matsumo.fanbox.feature.library.notify.LibraryNotifyRoute
import me.matsumo.fanbox.feature.library.notify.libraryNotifyScreen
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

@Composable
fun LibraryNavHost(
    navController: NavHostController,
    openDrawer: () -> Unit,
    navigateToPostSearch: () -> Unit,
    navigateToPostDetailFromHome: (postId: FanboxPostId) -> Unit,
    navigateToPostDetailFromSupported: (postId: FanboxPostId) -> Unit,
    navigateToCreatorPosts: (creatorId: FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (creatorId: FanboxCreatorId) -> Unit,
    navigateToSimpleAlert: (SimpleAlertContents) -> Unit,
    modifier: Modifier = Modifier,
    startDestination: String = LibraryHomeRoute,
    applyOtherRoutes: NavGraphBuilder.() -> Unit = {},
) {
    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = { NavigateAnimation.Home.enter },
        exitTransition = { NavigateAnimation.Home.exit },
    ) {
        libraryHomeScreen(
            openDrawer = openDrawer,
            navigateToPostDetailFromHome = navigateToPostDetailFromHome,
            navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
            navigateToCreatorPosts = navigateToCreatorPosts,
            navigateToCreatorPlans = navigateToCreatorPlans,
            navigateToSimpleAlert = navigateToSimpleAlert,
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
