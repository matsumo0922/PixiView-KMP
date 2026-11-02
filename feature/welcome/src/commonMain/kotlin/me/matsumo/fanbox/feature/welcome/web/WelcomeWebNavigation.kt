package me.matsumo.fanbox.feature.welcome.web

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents

fun EntryProviderScope<Destination>.welcomeWebEntry(
    navigateToLoginAlert: (SimpleAlertContents) -> Unit,
    navigateToLoginDebugAlert: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    entry<Destination.WelcomeWeb> {
        WelcomeWebScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToLoginAlert = navigateToLoginAlert,
            navigateToLoginDebugAlert = navigateToLoginDebugAlert,
            terminate = terminate,
        )
    }
}
