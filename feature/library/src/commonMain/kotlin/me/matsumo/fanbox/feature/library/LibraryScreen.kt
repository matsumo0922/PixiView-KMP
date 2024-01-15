package me.matsumo.fanbox.feature.library

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import caios.android.fanbox.feature.library.home.LibraryHomeRoute
import caios.android.fanbox.feature.library.home.navigateToLibraryHome
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.rememberNavigator
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.library.component.LibraryBottomBar
import me.matsumo.fanbox.feature.library.component.LibraryDestination
import me.matsumo.fanbox.feature.library.component.LibraryDrawer
import me.matsumo.fanbox.feature.library.component.LibraryNavigationRail
import me.matsumo.fanbox.feature.library.discovery.navigateToLibraryDiscovery
import me.matsumo.fanbox.feature.library.message.navigateToLibraryMessage
import me.matsumo.fanbox.feature.library.notify.navigateToLibraryNotify
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import moe.tlaster.precompose.navigation.NavOptions
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.PopUpTo

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
    viewModel: LibraryViewModel = koinViewModel(LibraryViewModel::class)
) {
    val navigator = rememberNavigator("Library")
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val navigationType = LocalNavigationType.current

    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val backStackEntry by navigator.currentEntry.collectAsStateWithLifecycle(null)

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) {
        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                LibraryDrawer(
                    state = drawerState,
                    userData = it.userData,
                    currentDestination = backStackEntry?.route?.route,
                    onClickLibrary = navigator::navigateToLibraryDestination,
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
            Row(Modifier.fillMaxSize()) {
                AnimatedVisibility(navigationType.type == PixiViewNavigationType.NavigationRail) {
                    LibraryNavigationRail(
                        modifier = Modifier.fillMaxHeight(),
                        destinations = LibraryDestination.entries.toImmutableList(),
                        currentDestination = backStackEntry?.route?.route,
                        navigateToDestination = navigator::navigateToLibraryDestination,
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                ) {
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
                                modifier = Modifier.weight(1f),
                                navController = navigator,
                                openDrawer = {
                                    // PreCompose bug https://github.com/Tlaster/PreCompose/issues/238

                                    scope.launch {
                                        drawerState.open()
                                    }
                                },
                                navigateToPostSearch = navigateToPostSearch,
                                navigateToPostDetailFromHome = navigateToPostDetailFromHome,
                                navigateToPostDetailFromSupported = navigateToPostDetailFromSupported,
                                navigateToCreatorPosts = navigateToCreatorPosts,
                                navigateToCreatorPlans = navigateToCreatorPlans,
                                navigateToCancelPlus = navigateToCancelPlus,
                            )
                        }
                    }

                    AnimatedVisibility(navigationType.type == PixiViewNavigationType.BottomNavigation) {
                        LibraryBottomBar(
                            modifier = Modifier.fillMaxWidth(),
                            destinations = LibraryDestination.entries.toImmutableList(),
                            currentDestination = backStackEntry?.route?.route,
                            navigateToDestination = navigator::navigateToLibraryDestination,
                        )
                    }
                }
            }
        }
    }
}

fun Navigator.navigateToLibraryDestination(destination: LibraryDestination) {
    val navOption = NavOptions(
        popUpTo = PopUpTo(LibraryHomeRoute),
        launchSingleTop = true,
    )

    when (destination) {
        LibraryDestination.Home -> navigateToLibraryHome(navOption)
        LibraryDestination.Discovery -> navigateToLibraryDiscovery(navOption)
        LibraryDestination.Notify -> navigateToLibraryNotify(navOption)
        LibraryDestination.Message -> navigateToLibraryMessage(navOption)
    }
}
