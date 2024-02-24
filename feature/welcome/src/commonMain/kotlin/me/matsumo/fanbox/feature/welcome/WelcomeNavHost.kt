package me.matsumo.fanbox.feature.welcome

import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import dev.icerock.moko.permissions.Permission
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.rememberNavigator
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
import moe.tlaster.precompose.navigation.NavHost
import moe.tlaster.precompose.navigation.transition.NavTransition

@Composable
fun WelcomeNavHost(
    onComplete: () -> Unit,
    isAgreedTeams: Boolean,
    isAllowedPermission: Boolean,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
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
            val navigator = rememberNavigator("Welcome")

            NavHost(
                modifier = modifier,
                navigator = navigator,
                initialRoute = startDestination,
                navTransition = remember {
                    NavTransition(
                        createTransition = slideInHorizontally { it },
                        destroyTransition = slideOutHorizontally { it },
                        pauseTransition = slideOutHorizontally { -it / 4 },
                        resumeTransition = slideInHorizontally { -it / 4 },
                        exitTargetContentZIndex = 1f
                    )
                },
            ) {
                welcomeTopScreen(
                    navigateToWelcomeLogin = { navigator.navigateToWelcomeLogin() },
                    navigateToForIos = { navigator.navigateToSimpleAlertDialog(it) },
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
                    navigateToLoginAlert = { navigator.navigateToSimpleAlertDialog(it) },
                    terminate = { navigator.goBack() }
                )

                welcomePermissionScreen(
                    navigateToHome = { onComplete.invoke() },
                )

                simpleAlertDialogDialog(
                    onResult = { navigator.goBackWith(it) }
                )
            }
        }

        BindEffect(controller)
    }
}
