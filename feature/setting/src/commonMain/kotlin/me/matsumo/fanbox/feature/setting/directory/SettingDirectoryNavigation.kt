package me.matsumo.fanbox.feature.setting.directory

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.settingDirectoryEntry(terminate: () -> Unit) {
    entry<Destination.SettingDirectory> {
        SettingDirectoryRoute(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
