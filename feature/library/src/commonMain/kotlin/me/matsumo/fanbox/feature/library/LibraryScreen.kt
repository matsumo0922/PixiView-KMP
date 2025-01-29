package me.matsumo.fanbox.feature.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavHostController
import androidx.navigation.navOptions
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.feature.library.component.LibraryCompactScreen
import me.matsumo.fanbox.feature.library.component.LibraryDestination
import me.matsumo.fanbox.feature.library.component.LibraryExpandedScreen
import me.matsumo.fanbox.feature.library.component.LibraryMediumScreen
import me.matsumo.fanbox.feature.library.discovery.navigateToLibraryDiscovery
import me.matsumo.fanbox.feature.library.home.navigateToLibraryHome
import me.matsumo.fanbox.feature.library.message.navigateToLibraryMessage
import me.matsumo.fanbox.feature.library.notify.navigateToLibraryNotify
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
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
    modifier: Modifier = Modifier,
    viewModel: LibraryViewModel = koinViewModel(),
) {
    val drawerState = rememberDrawerState(DrawerValue.Closed)

    val snackbarHostState = LocalSnackbarHostState.current
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
                    navController = navHostController,
                    navigateToPostSearch = navigateToPostSearch,
                    navigateToPostByCreatorSearch = navigateToPostByCreatorSearch,
                    navigateToPostDetailFromHome = navigateToPostDetailFromHome,
                    navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
                    navigateToCreatorPosts = navigateToCreatorPosts,
                    navigateToCreatorPlans = navigateToCreatorPlans,
                    navigateToBookmarkedPosts = navigateToBookmarkedPosts,
                    navigateToFollowerCreators = navigateToFollowerCreators,
                    navigateToSupportingCreators = navigateToSupportingCreators,
                    navigateToPayments = navigateToPayments,
                    navigateToDownloadQueue = navigateToDownloadQueue,
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
                    navController = navHostController,
                    navigateToPostSearch = navigateToPostSearch,
                    navigateToPostByCreatorSearch = navigateToPostByCreatorSearch,
                    navigateToPostDetailFromHome = navigateToPostDetailFromHome,
                    navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
                    navigateToCreatorPosts = navigateToCreatorPosts,
                    navigateToCreatorPlans = navigateToCreatorPlans,
                    navigateToBookmarkedPosts = navigateToBookmarkedPosts,
                    navigateToFollowerCreators = navigateToFollowerCreators,
                    navigateToSupportingCreators = navigateToSupportingCreators,
                    navigateToPayments = navigateToPayments,
                    navigateToDownloadQueue = navigateToDownloadQueue,
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
                    navController = navHostController,
                    navigateToPostSearch = navigateToPostSearch,
                    navigateToPostByCreatorSearch = navigateToPostByCreatorSearch,
                    navigateToPostDetailFromHome = navigateToPostDetailFromHome,
                    navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
                    navigateToCreatorPosts = navigateToCreatorPosts,
                    navigateToCreatorPlans = navigateToCreatorPlans,
                    navigateToBookmarkedPosts = navigateToBookmarkedPosts,
                    navigateToFollowerCreators = navigateToFollowerCreators,
                    navigateToSupportingCreators = navigateToSupportingCreators,
                    navigateToPayments = navigateToPayments,
                    navigateToDownloadQueue = navigateToDownloadQueue,
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
