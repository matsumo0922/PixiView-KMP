package me.matsumo.fanbox.feature.library.component

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
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
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.model.SimpleAlertContents
import me.matsumo.fanbox.feature.library.LibraryNavHost
import me.matsumo.fanbox.feature.library.LibraryUiState
import me.matsumo.fanbox.feature.library.navigateToLibraryDestination
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

@Composable
internal fun LibraryMediumScreen(
    uiState: LibraryUiState,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    navigateTo: (Destination) -> Unit,
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
                navigateTo = navigateTo,
            )
        },
    ) {
        Row(Modifier.fillMaxSize()) {
            if (!uiState.userData.isTestUser) {
                LibraryNavigationRail(
                    modifier = Modifier.fillMaxHeight(),
                    destinations = LibraryDestination.entries.toImmutableList(),
                    currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                    navigateToDestination = navController::navigateToLibraryDestination,
                )
            }

            Scaffold(
                modifier = Modifier.weight(1f),
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
                        userData = uiState.userData,
                        openDrawer = {
                            scope.launch {
                                drawerState.open()
                            }
                        },
                        navigateTo = navigateTo,
                    )
                }
            }
        }
    }
}
