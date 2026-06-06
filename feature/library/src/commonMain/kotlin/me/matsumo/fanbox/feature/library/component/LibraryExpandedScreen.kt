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
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.feature.library.LibraryNavHost
import me.matsumo.fanbox.feature.library.LibraryUiState
import me.matsumo.fanbox.feature.library.navigateToLibraryDestination

@Composable
internal fun LibraryExpandedScreen(
    uiState: LibraryUiState,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    navController: NavHostController,
    navigateTo: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

    PermanentNavigationDrawer(
        modifier = modifier,
        drawerContent = {
            LibraryPermanentDrawer(
                state = drawerState,
                setting = uiState.setting,
                currentDestination = navController.currentBackStackEntryAsState().value?.destination,
                onClickLibrary = navController::navigateToLibraryDestination,
                navigateTo = navigateTo,
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
                    setting = uiState.setting,
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
