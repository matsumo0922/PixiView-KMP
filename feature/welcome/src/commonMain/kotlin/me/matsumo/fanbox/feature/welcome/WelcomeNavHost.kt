package me.matsumo.fanbox.feature.welcome

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.feature.welcome.top.WelcomeTopRoute
import me.matsumo.fanbox.feature.welcome.top.welcomeTopScreen
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.rememberNavigator

@Composable
fun WelcomeNavHost(
    onComplete: () -> Unit,
    isAgreedTeams: Boolean,
    isAllowedPermission: Boolean,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberNavigator()

    NavHost(
        modifier = modifier,
        navigator = navigator,
        initialRoute = WelcomeTopRoute,
    ) {
        welcomeTopScreen(
            navigateToWelcomePlus = {  },
        )
    }
}
