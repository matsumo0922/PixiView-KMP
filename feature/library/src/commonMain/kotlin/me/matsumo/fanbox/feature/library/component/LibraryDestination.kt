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
import dev.icerock.moko.resources.StringResource
import me.matsumo.fanbox.core.ui.MR

enum class LibraryDestination(
    val selectedIcon: ImageVector,
    val deselectedIcon: ImageVector,
    val title: StringResource,
) {
    Home(
        selectedIcon = Icons.Default.Home,
        deselectedIcon = Icons.Outlined.Home,
        title = MR.strings.library_navigation_home,
    ),
    Discovery(
        selectedIcon = Icons.Default.Search,
        deselectedIcon = Icons.Outlined.Search,
        title = MR.strings.library_navigation_discovery,
    ),
    Notify(
        selectedIcon = Icons.Default.Notifications,
        deselectedIcon = Icons.Outlined.Notifications,
        title = MR.strings.library_navigation_notify,
    ),
    Message(
        selectedIcon = Icons.Default.Mail,
        deselectedIcon = Icons.Outlined.Mail,
        title = MR.strings.library_navigation_message,
    ),
}
