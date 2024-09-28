package me.matsumo.fanbox.feature.welcome

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import kotlinx.datetime.Clock
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.logs.category.WelcomeLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.popBackStackWithResult
import me.matsumo.fanbox.core.ui.view.navigateToSimpleAlertDialog
import me.matsumo.fanbox.core.ui.view.simpleAlertDialogDialog
import me.matsumo.fanbox.feature.welcome.login.WelcomeLoginRoute
import me.matsumo.fanbox.feature.welcome.login.navigateToWelcomeLogin
import me.matsumo.fanbox.feature.welcome.login.welcomeLoginScreen
import me.matsumo.fanbox.feature.welcome.top.WelcomeTopRoute
import me.matsumo.fanbox.feature.welcome.top.welcomeTopScreen
import me.matsumo.fanbox.feature.welcome.web.navigateToWelcomeWeb
import me.matsumo.fanbox.feature.welcome.web.welcomeWebScreen

@Composable
fun WelcomeNavHost(
    onComplete: () -> Unit,
    isAgreedTeams: Boolean,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }
    val snackbarHostState = remember { SnackbarHostState() }

    val startDestination = when {
        !isAgreedTeams -> WelcomeTopRoute
        !isLoggedIn -> WelcomeLoginRoute
        else -> WelcomeTopRoute
    }

    val onboardingStartTime = Clock.System.now()

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
                modifier = Modifier.fillMaxSize(),
                navController = navController,
                startDestination = startDestination,
                enterTransition = { NavigateAnimation.Horizontal.enter },
                exitTransition = { NavigateAnimation.Horizontal.exit },
                popEnterTransition = { NavigateAnimation.Horizontal.popEnter },
                popExitTransition = { NavigateAnimation.Horizontal.popExit },
            ) {
                welcomeTopScreen(
                    navigateToWelcomeLogin = {
                        WelcomeLog.firstOpen().send()
                        navController.navigateToWelcomeLogin()
                    },
                )

                welcomeLoginScreen(
                    navigateToLoginScreen = { navController.navigateToWelcomeWeb() },
                    navigateToHome = {
                        val onboardingEndTime = Clock.System.now()

                        WelcomeLog.completedOnboarding(
                            startAt = onboardingStartTime.format("%Y-%m-%d %H:%M:%S"),
                            endAt = onboardingEndTime.format("%Y-%m-%d %H:%M:%S"),
                            neededTime = onboardingEndTime.epochSeconds - onboardingStartTime.epochSeconds,
                        ).send()

                        onComplete.invoke()
                    },
                )

                welcomeWebScreen(
                    navigateToLoginAlert = { navController.navigateToSimpleAlertDialog(it) },
                    navigateToLoginDebugAlert = { contents, onPositive -> navController.navigateToSimpleAlertDialog(contents, onPositive) },
                    terminate = { navController.popBackStack() },
                )

                simpleAlertDialogDialog(
                    onResult = { navController.popBackStackWithResult(it) },
                )
            }
        }

        BindEffect(controller)
    }
}
