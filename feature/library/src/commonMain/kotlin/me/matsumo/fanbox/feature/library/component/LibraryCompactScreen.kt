package me.matsumo.fanbox.feature.library.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.DrawerState
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.ads.BannerAdView
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.library.LibraryNavHost
import me.matsumo.fanbox.feature.library.LibraryUiState
import me.matsumo.fanbox.feature.library.navigateToLibraryDestination

@Composable
internal fun LibraryCompactScreen(
    uiState: LibraryUiState,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
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
    navigateToBillingPlus: (String?) -> Unit,
    navigateToCancelPlus: (SimpleAlertContents) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        modifier = modifier,
        drawerState = drawerState,
        drawerContent = {
            LibraryDrawer(
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
        Column {
            Scaffold(
                modifier = Modifier.weight(1f),
                snackbarHost = {
                    SnackbarHost(
                        modifier = Modifier.navigationBarsPadding(),
                        hostState = snackbarHostState,
                    )
                }
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

            LibraryBottomBar(
                modifier = Modifier.fillMaxWidth(),
                destinations = LibraryDestination.entries.toImmutableList(),
                currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                navigateToDestination = navController::navigateToLibraryDestination,
            )
        }
    }
}