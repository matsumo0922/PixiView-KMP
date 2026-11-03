package me.matsumo.fanbox.feature.setting.oss

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.settingLicenseEntry(terminate: () -> Unit) {
    entry<Destination.SettingLicense> {
        SettingLicenseScreen(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
