package me.matsumo.fanbox.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import me.matsumo.fanbox.MainUiState
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.ui.component.sheet.rememberBottomSheetNavigator
import me.matsumo.fanbox.feature.welcome.WelcomeNavHost
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
internal fun PixiViewScreen(
    uiState: MainUiState,
    onRequestInitPixiViewId: () -> Unit,
    onRequestFirstLaunchFlag: () -> Unit,
    onRequestUpdateState: () -> Unit,
    onBillingRetentionPromptShown: () -> Unit,
    onPostDetailClosed: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)

    var showPaywallFlag by remember { mutableStateOf(false) }
    var isAgreedTeams by remember {
        mutableStateOf(uiState.setting.isAgreedPrivacyPolicy && uiState.setting.isAgreedTermsOfService)
    }
    var isAllowedPermission by remember(uiState.setting, uiState.isLoggedIn) { mutableStateOf(true) }

    LaunchedEffect(true) {
        if (uiState.setting.pixiViewId.isBlank()) {
            onRequestInitPixiViewId.invoke()
        }

        if (uiState.setting.firstLaunchTime == -1L) {
            onRequestFirstLaunchFlag.invoke()
        }
    }

    AnimatedContent(
        modifier = modifier,
        targetState = !isAgreedTeams || !uiState.isLoggedIn || !isAllowedPermission,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "isShowWelcomeScreen",
    ) {
        if (it) {
            WelcomeNavHost(
                isAgreedTeams = isAgreedTeams,
                isLoggedIn = uiState.isLoggedIn,
                onComplete = {
                    isAgreedTeams = true
                    isAllowedPermission = true
                    showPaywallFlag = true

                    onRequestUpdateState.invoke()
                },
            )
        } else {
            PixiViewContent(
                modifier = Modifier.fillMaxSize(),
                bottomSheetNavigator = bottomSheetNavigator,
                navController = navController,
                onPostDetailClosed = onPostDetailClosed,
            )

            HandleBillingRetentionPrompt(
                uiState = uiState,
                navController = navController,
                onBillingRetentionPromptShown = onBillingRetentionPromptShown,
            )

            if (showPaywallFlag) {
                LaunchedEffect(true) {
                    navController.navigate(Destination.BillingPlusBottomSheet(null))
                    showPaywallFlag = false
                }
            }
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
private fun HandleBillingRetentionPrompt(
    uiState: MainUiState,
    navController: NavHostController,
    onBillingRetentionPromptShown: () -> Unit,
) {
    val currentOnBillingRetentionPromptShown by rememberUpdatedState(onBillingRetentionPromptShown)
    var shownPromptEpisodeKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.setting, uiState.isBillingSyncSucceeded, uiState.isAppLocked) {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val canShowPrompt = uiState.canShowBillingRetentionPrompt(currentTimeMillis)
        if (!canShowPrompt) return@LaunchedEffect

        val promptEpisodeKey = uiState.setting.billingRetentionPromptEpisodeKey()
        if (shownPromptEpisodeKey == promptEpisodeKey) return@LaunchedEffect

        shownPromptEpisodeKey = promptEpisodeKey
        currentOnBillingRetentionPromptShown()
        navController.navigate(
            Destination.BillingRetentionBottomSheet(
                isAnnualOfferShown = uiState.setting.shouldShowBillingRetentionAnnualOffer,
            ),
        )
    }
}

private fun MainUiState.canShowBillingRetentionPrompt(currentTimeMillis: Long): Boolean {
    if (!isBillingSyncSucceeded) return false
    if (isAppLocked) return false

    return setting.canShowBillingRetentionPrompt(currentTimeMillis)
}

private fun Setting.billingRetentionPromptEpisodeKey(): String {
    return plusUnsubscribeDetectedAtMillis?.toString() ?: BILLING_RETENTION_PROMPT_UNKNOWN_EPISODE_KEY
}

/** 解約検知時刻がないリテンション表示履歴のエピソードキー。 */
private const val BILLING_RETENTION_PROMPT_UNKNOWN_EPISODE_KEY = "unknown"
