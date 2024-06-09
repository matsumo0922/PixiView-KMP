package me.matsumo.fanbox.feature.welcome

import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.popBackStackWithResult
import me.matsumo.fanbox.core.ui.view.navigateToSimpleAlertDialog
import me.matsumo.fanbox.core.ui.view.simpleAlertDialogDialog
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

@Composable
fun WelcomeNavHost(
    onComplete: () -> Unit,
    isAgreedTeams: Boolean,
    isAllowedPermission: Boolean,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()
    val scope = rememberCoroutineScope()

    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }
    val snackbarHostState = remember { SnackbarHostState() }

    val startDestination = when {
        !isAgreedTeams -> WelcomeTopRoute
        !isLoggedIn -> WelcomeLoginRoute
        !isAllowedPermission -> WelcomePermissionRoute
        else -> WelcomeTopRoute
    }

    CompositionLocalProvider(LocalSnackbarHostState provides snackbarHostState) {
        Scaffold(
            modifier = modifier,
            snackbarHost = {
                SnackbarHost(
                    modifier = Modifier.navigationBarsPadding(),
                    hostState = snackbarHostState,
                )
            },
        ) {
            NavHost(
                modifier = modifier,
                navController = navController,
                startDestination = startDestination,
                enterTransition = { NavigateAnimation.Horizontal.enter },
                exitTransition = { NavigateAnimation.Horizontal.exit },
                popEnterTransition = { NavigateAnimation.Horizontal.popEnter },
                popExitTransition = { NavigateAnimation.Horizontal.popExit },
            ) {
                welcomeTopScreen(
                    navigateToWelcomeLogin = { navController.navigateToWelcomeLogin() },
                )

                welcomeLoginScreen(
                    navigateToLoginScreen = { navController.navigateToWelcomeWeb() },
                    navigateToWelcomePermission = {
                        scope.launch {
                            if (controller.isPermissionGranted(Permission.STORAGE)) {
                                onComplete.invoke()
                            } else {
                                navController.navigateToWelcomePermission()
                            }
                        }
                    },
                )

                welcomeWebScreen(
                    navigateToLoginAlert = { navController.navigateToSimpleAlertDialog(it) },
                    terminate = { navController.popBackStack() }
                )

                welcomePermissionScreen(
                    navigateToHome = { onComplete.invoke() },
                )

                simpleAlertDialogDialog(
                    onResult = { navController.popBackStackWithResult(it) }
                )
            }
        }

        BindEffect(controller)
    }
}
