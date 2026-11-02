package me.matsumo.fanbox.feature.setting.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents

fun EntryProviderScope<Destination>.settingTopEntry(
    navigateTo: (Destination) -> Unit,
    navigateToLogoutDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    entry<Destination.SettingTop> {
        SettingTopRoute(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            navigateToLogoutDialog = navigateToLogoutDialog,
            terminate = terminate,
        )
    }
}
