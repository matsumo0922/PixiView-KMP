package me.matsumo.fanbox.feature.library

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.feature.library.discovery.LibraryDiscoveryRoute
import me.matsumo.fanbox.feature.library.discovery.libraryDiscoveryScreen
import me.matsumo.fanbox.feature.library.home.LibraryHomeRoute
import me.matsumo.fanbox.feature.library.home.libraryHomeScreen
import me.matsumo.fanbox.feature.library.message.LibraryMessageRoute
import me.matsumo.fanbox.feature.library.message.libraryMessageScreen
import me.matsumo.fanbox.feature.library.notify.LibraryNotifyRoute
import me.matsumo.fanbox.feature.library.notify.libraryNotifyScreen

@Composable
fun LibraryNavHost(
    navController: NavHostController,
    setting: Setting,
    openDrawer: () -> Unit,
    navigateTo: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val startDestination = if (setting.isTestUser) {
        LibraryDiscoveryRoute
    } else {
        LibraryHomeRoute
    }

    NavHost(
        modifier = modifier,
        navController = navController,
        startDestination = startDestination,
        enterTransition = { NavigateAnimation.Home.enter },
        exitTransition = { NavigateAnimation.Home.exit },
    ) {
        libraryHomeScreen(
            openDrawer = openDrawer,
            navigateTo = navigateTo,
        )

        libraryDiscoveryScreen(
            openDrawer = openDrawer,
            navigateTo = navigateTo,
        )

        libraryNotifyScreen(
            openDrawer = openDrawer,
            navigateTo = navigateTo,
        )

        libraryMessageScreen(
            openDrawer = openDrawer,
            navigateTo = navigateTo,
        )
    }
}

internal val LibraryRoutes = listOf(
    LibraryHomeRoute,
    LibraryDiscoveryRoute,
    LibraryNotifyRoute,
    LibraryMessageRoute,
)
