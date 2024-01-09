package me.matsumo.fanbox.feature.welcome

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.extensition.rememberNavigator
import me.matsumo.fanbox.feature.welcome.login.WelcomeLoginRoute
import me.matsumo.fanbox.feature.welcome.login.navigateToWelcomeLogin
import me.matsumo.fanbox.feature.welcome.login.welcomeLoginScreen
import me.matsumo.fanbox.feature.welcome.permission.WelcomePermissionRoute
import me.matsumo.fanbox.feature.welcome.permission.navigateToWelcomePermission
import me.matsumo.fanbox.feature.welcome.permission.welcomePermissionScreen
import me.matsumo.fanbox.feature.welcome.top.WelcomeTopRoute
import me.matsumo.fanbox.feature.welcome.top.welcomeTopScreen
import me.matsumo.fanbox.feature.welcome.web.navigateToWelcomeWeb
import me.matsumo.fanbox.feature.welcome.web.welcomeWebScreen
import moe.tlaster.precompose.navigation.NavHost

@Composable
fun WelcomeNavHost(
    onComplete: () -> Unit,
    isAgreedTeams: Boolean,
    isAllowedPermission: Boolean,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()
    val navigator = rememberNavigator("Welcome")

    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }

    val startDestination = when {
        !isAgreedTeams -> WelcomeTopRoute
        !isLoggedIn -> WelcomeLoginRoute
        !isAllowedPermission -> WelcomePermissionRoute
        else -> WelcomeTopRoute
    }

    NavHost(
        modifier = modifier,
        navigator = navigator,
        initialRoute = startDestination,
    ) {
        welcomeTopScreen(
            navigateToWelcomeLogin = { navigator.navigateToWelcomeLogin() },
        )

        welcomeLoginScreen(
            navigateToLoginScreen = { navigator.navigateToWelcomeWeb() },
            navigateToWelcomePermission = {
                scope.launch {
                    if (controller.isPermissionGranted(Permission.STORAGE)) {
                        onComplete.invoke()
                    } else {
                        navigator.navigateToWelcomePermission()
                    }
                }
            },
        )

        welcomeWebScreen(
            terminate = { navigator.goBack() }
        )

        welcomePermissionScreen(
            navigateToHome = { onComplete.invoke() },
        )
    }

    BindEffect(controller)
}
