package me.matsumo.fanbox.feature.library.component

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.PermanentNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.library.LibraryNavHost
import me.matsumo.fanbox.feature.library.LibraryUiState
import me.matsumo.fanbox.feature.library.navigateToLibraryDestination
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

@Composable
internal fun LibraryExpandedScreen(
    uiState: LibraryUiState,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    navigateToPostSearch: () -> Unit,
    navigateToPostDetailFromHome: (postId: FanboxPostId) -> Unit,
    navigateToPostDetailFromSupported: (postId: FanboxPostId) -> Unit,
    navigateToCreatorPosts: (creatorId: FanboxCreatorId) -> Unit,
    navigateToCreatorPlans: (creatorId: FanboxCreatorId) -> Unit,
    navigateToBookmarkedPosts: () -> Unit,
    navigateToFollowerCreators: () -> Unit,
    navigateToSupportingCreators: () -> Unit,
    navigateToPayments: () -> Unit,
    navigateToSettingTop: () -> Unit,
    navigateToAbout: () -> Unit,
    navigateToBillingPlus: (String?) -> Unit,
    navigateToCancelPlus: (SimpleAlertContents) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            LibraryPermanentDrawer(
                state = drawerState,
                userData = uiState.userData,
                currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                onClickLibrary = navController::navigateToLibraryDestination,
                navigateToBookmarkedPosts = navigateToBookmarkedPosts,
                navigateToFollowingCreators = navigateToFollowerCreators,
                navigateToSupportingCreators = navigateToSupportingCreators,
                navigateToPayments = navigateToPayments,
                navigateToSetting = navigateToSettingTop,
                navigateToAbout = navigateToAbout,
                navigateToBillingPlus = navigateToBillingPlus,
            )
        },
    ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            snackbarHost = {
                SnackbarHost(
                    modifier = Modifier.navigationBarsPadding(),
                    hostState = snackbarHostState,
                )
            },
        ) {
            CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
                LibraryNavHost(
                    modifier = Modifier.fillMaxSize(),
                    navController = navController,
                    openDrawer = {
                        scope.launch {
                            drawerState.open()
                        }
                    },
                    navigateToPostSearch = navigateToPostSearch,
                    navigateToPostDetailFromHome = navigateToPostDetailFromHome,
                    navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
                    navigateToCreatorPosts = navigateToCreatorPosts,
                    navigateToCreatorPlans = navigateToCreatorPlans,
                    navigateToSimpleAlert = navigateToCancelPlus,
                )
            }
        }
    }
}
