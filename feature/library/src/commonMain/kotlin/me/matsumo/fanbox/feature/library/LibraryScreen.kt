package me.matsumo.fanbox.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.NavOptions
import androidx.navigation.Navigator
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navOptions
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.ads.BannerAdView
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.NavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.library.component.LibraryBottomBar
import me.matsumo.fanbox.feature.library.component.LibraryCompactScreen
import me.matsumo.fanbox.feature.library.component.LibraryDestination
import me.matsumo.fanbox.feature.library.component.LibraryDrawer
import me.matsumo.fanbox.feature.library.component.LibraryExpandedScreen
import me.matsumo.fanbox.feature.library.component.LibraryMediumScreen
import me.matsumo.fanbox.feature.library.component.LibraryNavigationRail
import me.matsumo.fanbox.feature.library.discovery.navigateToLibraryDiscovery
import me.matsumo.fanbox.feature.library.home.navigateToLibraryHome
import me.matsumo.fanbox.feature.library.message.navigateToLibraryMessage
import me.matsumo.fanbox.feature.library.notify.navigateToLibraryNotify
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
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
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel()
) {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }

    val navigationType = LocalNavigationType.current.type
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) {
        when (navigationType) {
            PixiViewNavigationType.BottomNavigation -> {
                LibraryCompactScreen(
                    modifier = Modifier.fillMaxSize(),
                    uiState = it,
                    drawerState = drawerState,
                    snackbarHostState = snackbarHostState,
                    navController = navController,
                    navigateToPostSearch = navigateToPostSearch,
                    navigateToPostDetailFromHome = navigateToPostDetailFromHome,
                    navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
                    navigateToCreatorPosts = navigateToCreatorPosts,
                    navigateToCreatorPlans = navigateToCreatorPlans,
                    navigateToBookmarkedPosts = navigateToBookmarkedPosts,
                    navigateToFollowerCreators = navigateToFollowerCreators,
                    navigateToSupportingCreators = navigateToSupportingCreators,
                    navigateToPayments = navigateToPayments,
                    navigateToSettingTop = navigateToSettingTop,
                    navigateToAbout = navigateToAbout,
                    navigateToBillingPlus = navigateToBillingPlus,
                    navigateToCancelPlus = navigateToCancelPlus,
                )
            }
            PixiViewNavigationType.NavigationRail -> {
                LibraryMediumScreen(
                    modifier = Modifier.fillMaxSize(),
                    uiState = it,
                    drawerState = drawerState,
                    snackbarHostState = snackbarHostState,
                    navController = navController,
                    navigateToPostSearch = navigateToPostSearch,
                    navigateToPostDetailFromHome = navigateToPostDetailFromHome,
                    navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
                    navigateToCreatorPosts = navigateToCreatorPosts,
                    navigateToCreatorPlans = navigateToCreatorPlans,
                    navigateToBookmarkedPosts = navigateToBookmarkedPosts,
                    navigateToFollowerCreators = navigateToFollowerCreators,
                    navigateToSupportingCreators = navigateToSupportingCreators,
                    navigateToPayments = navigateToPayments,
                    navigateToSettingTop = navigateToSettingTop,
                    navigateToAbout = navigateToAbout,
                    navigateToBillingPlus = navigateToBillingPlus,
                    navigateToCancelPlus = navigateToCancelPlus,
                )
            }
            PixiViewNavigationType.PermanentNavigationDrawer -> {
                LibraryExpandedScreen(
                    modifier = Modifier.fillMaxSize(),
                    uiState = it,
                    drawerState = drawerState,
                    snackbarHostState = snackbarHostState,
                    navController = navController,
                    navigateToPostSearch = navigateToPostSearch,
                    navigateToPostDetailFromHome = navigateToPostDetailFromHome,
                    navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
                    navigateToCreatorPosts = navigateToCreatorPosts,
                    navigateToCreatorPlans = navigateToCreatorPlans,
                    navigateToBookmarkedPosts = navigateToBookmarkedPosts,
                    navigateToFollowerCreators = navigateToFollowerCreators,
                    navigateToSupportingCreators = navigateToSupportingCreators,
                    navigateToPayments = navigateToPayments,
                    navigateToSettingTop = navigateToSettingTop,
                    navigateToAbout = navigateToAbout,
                    navigateToBillingPlus = navigateToBillingPlus,
                    navigateToCancelPlus = navigateToCancelPlus,
                )
            }
        }
    }
}

fun NavHostController.navigateToLibraryDestination(destination: LibraryDestination) {
    val navOption = navOptions {
        popUpTo(graph.findStartDestination().route.orEmpty()) {
            saveState = true
        }
        launchSingleTop = true
        restoreState = true
    }

    when (destination) {
        LibraryDestination.Home -> navigateToLibraryHome(navOption)
        LibraryDestination.Discovery -> navigateToLibraryDiscovery(navOption)
        LibraryDestination.Notify -> navigateToLibraryNotify(navOption)
        LibraryDestination.Message -> navigateToLibraryMessage(navOption)
    }
}
