package me.matsumo.fanbox.feature.setting.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.settingThemeEntry(navigateTo: (Destination) -> Unit, terminate: () -> Unit) {
    entry<Destination.SettingTheme> {
        SettingThemeRoute(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
