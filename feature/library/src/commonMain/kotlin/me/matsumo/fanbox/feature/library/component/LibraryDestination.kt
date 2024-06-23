package me.matsumo.fanbox.feature.library.component

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Mail
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Search
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hierarchy
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.library_navigation_discovery
import me.matsumo.fanbox.core.ui.library_navigation_home
import me.matsumo.fanbox.core.ui.library_navigation_message
import me.matsumo.fanbox.core.ui.library_navigation_notify
import org.jetbrains.compose.resources.StringResource

enum class LibraryDestination(
    val selectedIcon: ImageVector,
    val deselectedIcon: ImageVector,
    val title: StringResource,
) {
    Home(
        selectedIcon = Icons.Default.Home,
        deselectedIcon = Icons.Outlined.Home,
        title = Res.string.library_navigation_home,
    ),
    Discovery(
        selectedIcon = Icons.Default.Search,
        deselectedIcon = Icons.Outlined.Search,
        title = Res.string.library_navigation_discovery,
    ),
    Notify(
        selectedIcon = Icons.Default.Notifications,
        deselectedIcon = Icons.Outlined.Notifications,
        title = Res.string.library_navigation_notify,
    ),
    Message(
        selectedIcon = Icons.Default.Mail,
        deselectedIcon = Icons.Outlined.Mail,
        title = Res.string.library_navigation_message,
    ),
}

internal fun NavDestination?.isLibraryDestinationInHierarchy(destination: LibraryDestination): Boolean {
    return this?.hierarchy?.any { it.route?.contains(destination.name, true) ?: false } == true
}