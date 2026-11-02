package me.matsumo.fanbox.feature.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DrawerState
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.feature.library.component.LibraryCompactScreen
import me.matsumo.fanbox.feature.library.component.LibraryDestination
import me.matsumo.fanbox.feature.library.component.LibraryExpandedScreen
import me.matsumo.fanbox.feature.library.component.LibraryMediumScreen
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LibraryScreen(
    navigateTo: (Destination) -> Unit,
    onHomeVisibilityChange: (Boolean) -> Unit,
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
        LibraryContent(
            modifier = Modifier.fillMaxSize(),
            uiState = it,
            navigationType = navigationType,
            drawerState = drawerState,
            snackbarHostState = snackbarHostState,
            navigateTo = navigateTo,
            onHomeVisibilityChange = onHomeVisibilityChange,
        )
    }
}

@Composable
private fun LibraryContent(
    uiState: LibraryUiState,
    navigationType: PixiViewNavigationType,
    drawerState: DrawerState,
    snackbarHostState: SnackbarHostState,
    navigateTo: (Destination) -> Unit,
    onHomeVisibilityChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val startTab = if (uiState.setting.isTestUser) LibraryDestination.Discovery else LibraryDestination.Home
    val tabState = rememberLibraryTabState(startTab)

    SideEffect {
        onHomeVisibilityChange(tabState.currentTab == LibraryDestination.Home)
    }

    when (navigationType) {
        PixiViewNavigationType.BottomNavigation -> {
            LibraryCompactScreen(
                modifier = modifier,
                uiState = uiState,
                drawerState = drawerState,
                snackbarHostState = snackbarHostState,
                tabState = tabState,
                navigateTo = navigateTo,
            )
        }

        PixiViewNavigationType.NavigationRail -> {
            LibraryMediumScreen(
                modifier = modifier,
                uiState = uiState,
                drawerState = drawerState,
                snackbarHostState = snackbarHostState,
                tabState = tabState,
                navigateTo = navigateTo,
            )
        }

        PixiViewNavigationType.PermanentNavigationDrawer -> {
            LibraryExpandedScreen(
                modifier = modifier,
                uiState = uiState,
                drawerState = drawerState,
                snackbarHostState = snackbarHostState,
                tabState = tabState,
                navigateTo = navigateTo,
            )
        }
    }
}
