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
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.library_navigation_discovery
import me.matsumo.fanbox.core.resources.library_navigation_home
import me.matsumo.fanbox.core.resources.library_navigation_message
import me.matsumo.fanbox.core.resources.library_navigation_notify
import org.jetbrains.compose.resources.StringResource

/**
 * Library のタブ。
 *
 * @property route 遷移ログに記録するタブの名前。
 */
enum class LibraryDestination(
    val route: String,
    val selectedIcon: ImageVector,
    val deselectedIcon: ImageVector,
    val title: StringResource,
) {
    Home(
        route = "libraryHome",
        selectedIcon = Icons.Default.Home,
        deselectedIcon = Icons.Outlined.Home,
        title = Res.string.library_navigation_home,
    ),
    Discovery(
        route = "libraryDiscovery",
        selectedIcon = Icons.Default.Search,
        deselectedIcon = Icons.Outlined.Search,
        title = Res.string.library_navigation_discovery,
    ),
    Notify(
        route = "libraryNotify",
        selectedIcon = Icons.Default.Notifications,
        deselectedIcon = Icons.Outlined.Notifications,
        title = Res.string.library_navigation_notify,
    ),
    Message(
        route = "libraryMessage",
        selectedIcon = Icons.Default.Mail,
        deselectedIcon = Icons.Outlined.Mail,
        title = Res.string.library_navigation_message,
    ),
}
