package me.matsumo.fanbox.feature.library.component

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.DrawerState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.PermanentDrawerSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.UserData

@Composable
fun LibraryDrawer(
    state: DrawerState,
    userData: UserData?,
    currentDestination: NavDestination?,
    onClickLibrary: (LibraryDestination) -> Unit,
    navigateTo: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    ModalDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        DrawerContent(
            modifier = modifier,
            state = state,
            userData = userData,
            currentDestination = currentDestination,
            onClickLibrary = onClickLibrary,
            navigateTo = navigateTo,
        )
    }
}

@Composable
fun LibraryPermanentDrawer(
    state: DrawerState,
    userData: UserData?,
    currentDestination: NavDestination?,
    onClickLibrary: (LibraryDestination) -> Unit,
    navigateTo: (Destination) -> Unit,
    modifier: Modifier = Modifier,
) {
    PermanentDrawerSheet(
        drawerContainerColor = MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets(0, 0, 0, 0),
    ) {
        DrawerContent(
            modifier = modifier,
            state = state,
            userData = userData,
            currentDestination = currentDestination,
            onClickLibrary = onClickLibrary,
            navigateTo = navigateTo,
        )
    }
}
