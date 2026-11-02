package me.matsumo.fanbox.feature.welcome.login

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.welcomeLoginEntry(
    navigateToLoginScreen: () -> Unit,
    navigateToHome: () -> Unit,
    navigateToSetting: () -> Unit,
) {
    entry<Destination.WelcomeLogin> {
        WelcomeLoginScreen(
            modifier = Modifier.fillMaxSize(),
            navigateToLoginScreen = navigateToLoginScreen,
            navigateToHome = navigateToHome,
            navigateToSetting = navigateToSetting,
        )
    }
}
