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
import dev.icerock.moko.permissions.PermissionsController
import dev.icerock.moko.permissions.compose.BindEffect
import dev.icerock.moko.permissions.compose.PermissionsControllerFactory
import dev.icerock.moko.permissions.compose.rememberPermissionsControllerFactory
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.logs.category.WelcomeLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.navigation.PixiViewNavDisplay
import me.matsumo.fanbox.core.ui.navigation.destinationEntryProvider
import me.matsumo.fanbox.core.ui.navigation.rememberPixiViewNavigator
import me.matsumo.fanbox.core.ui.view.navigateToSimpleAlertDialog
import me.matsumo.fanbox.core.ui.view.simpleAlertDialogEntry
import me.matsumo.fanbox.feature.welcome.login.welcomeLoginEntry
import me.matsumo.fanbox.feature.welcome.top.welcomeTopEntry
import me.matsumo.fanbox.feature.welcome.web.welcomeWebEntry
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
@Composable
fun WelcomeNavHost(
    onComplete: () -> Unit,
    navigateToSetting: () -> Unit,
    isAgreedTeams: Boolean,
    isLoggedIn: Boolean,
    modifier: Modifier = Modifier,
) {
    val factory: PermissionsControllerFactory = rememberPermissionsControllerFactory()
    val controller: PermissionsController = remember(factory) { factory.createPermissionsController() }
    val snackbarHostState = remember { SnackbarHostState() }

    val startDestination = when {
        !isAgreedTeams -> Destination.WelcomeTop
        !isLoggedIn -> Destination.WelcomeLogin
        else -> Destination.WelcomeTop
    }
    val navigator = rememberPixiViewNavigator(startDestination)

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
            PixiViewNavDisplay(
                modifier = Modifier.fillMaxSize(),
                navigator = navigator,
                entryProvider = destinationEntryProvider {
                    welcomeTopEntry(
                        navigateToWelcomeLogin = {
                            WelcomeLog.firstOpen().send()
                            navigator.navigate(Destination.WelcomeLogin)
                        },
                        navigateToSetting = navigateToSetting,
                    )

                    welcomeLoginEntry(
                        navigateToLoginScreen = { navigator.navigate(Destination.WelcomeWeb) },
                        navigateToHome = {
                            val onboardingEndTime = Clock.System.now()

                            WelcomeLog.completedOnboarding(
                                startAt = onboardingStartTime.format("%Y-%m-%d %H:%M:%S"),
                                endAt = onboardingEndTime.format("%Y-%m-%d %H:%M:%S"),
                                neededTime = onboardingEndTime.epochSeconds - onboardingStartTime.epochSeconds,
                            ).send()

                            onComplete.invoke()
                        },
                        navigateToSetting = navigateToSetting,
                    )

                    welcomeWebEntry(
                        navigateToLoginAlert = { navigator.navigateToSimpleAlertDialog(it) },
                        navigateToLoginDebugAlert = { contents, onPositive ->
                            navigator.navigateToSimpleAlertDialog(contents, onPositive)
                        },
                        terminate = navigator::back,
                    )

                    simpleAlertDialogEntry(
                        onResult = { navigator.backWithResult(it) },
                    )
                },
            )
        }

        BindEffect(controller)
    }
}
