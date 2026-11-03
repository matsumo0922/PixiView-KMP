package me.matsumo.fanbox.feature.welcome.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.welcomeTopEntry(navigateToWelcomeLogin: () -> Unit, navigateToSetting: () -> Unit) {
    entry<Destination.WelcomeTop> {
        WelcomeTopScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToWelcomeLogin = navigateToWelcomeLogin,
            navigateToSetting = navigateToSetting,
        )
    }
}
