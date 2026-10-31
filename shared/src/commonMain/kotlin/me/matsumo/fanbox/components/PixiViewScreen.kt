package me.matsumo.fanbox.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveableStateHolder
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import me.matsumo.fanbox.MainUiState
import me.matsumo.fanbox.PixiViewSettingNavHost
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.component.sheet.rememberBottomSheetNavigator
import me.matsumo.fanbox.feature.welcome.WelcomeNavHost
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@Composable
// Paywall への遷移を composition 適用後に dispatch し、画面離脱時はキャンセルする。
@Suppress("UnnecessaryLaunchedEffect")
internal fun PixiViewScreen(
    uiState: MainUiState,
    onRequestInitPixiViewId: () -> Unit,
    onRequestFirstLaunchFlag: () -> Unit,
    onRequestUpdateState: () -> Unit,
    onBillingRetentionPromptShow: () -> Unit,
    onPostDetailClose: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bottomSheetNavigator = rememberBottomSheetNavigator()
    val navController = rememberNavController(bottomSheetNavigator)
    val currentBackStackEntry by navController.currentBackStackEntryAsState()

    var showPaywallFlag by remember { mutableStateOf(false) }
    var isLibraryHomeVisible by remember { mutableStateOf(false) }
    var isAgreedTeams by remember {
        mutableStateOf(uiState.setting.isAgreedPrivacyPolicy && uiState.setting.isAgreedTermsOfService)
    }
    var isAllowedPermission by remember(uiState.setting, uiState.isLoggedIn) { mutableStateOf(true) }

    SideEffect(true) {
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
            WelcomeContent(
                modifier = Modifier.fillMaxSize(),
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
                onPostDetailClose = onPostDetailClose,
                onLibraryHomeVisibilityChange = { isVisible -> isLibraryHomeVisible = isVisible },
            )

            HandleBillingRetentionPrompt(
                uiState = uiState,
                navController = navController,
                isHomeVisible = isLibraryHomeVisible && currentBackStackEntry?.destination?.hasRoute<Destination.Library>() == true,
                onBillingRetentionPromptShow = onBillingRetentionPromptShow,
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

/**
 * ログイン前の画面と、そこから開く設定画面を切り替える。
 *
 * 設定画面を開いているあいだ welcome の画面は composition から外れるため、閉じたときに元の画面へ戻れるよう状態を保存しておく。
 */
@Composable
private fun WelcomeContent(
    isAgreedTeams: Boolean,
    isLoggedIn: Boolean,
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val saveableStateHolder = rememberSaveableStateHolder()
    var isSettingVisible by remember { mutableStateOf(false) }

    if (isSettingVisible) {
        PixiViewSettingNavHost(
            modifier = modifier,
            onClose = { isSettingVisible = false },
        )
    } else {
        saveableStateHolder.SaveableStateProvider("welcome") {
            WelcomeNavHost(
                modifier = modifier,
                isAgreedTeams = isAgreedTeams,
                isLoggedIn = isLoggedIn,
                onComplete = onComplete,
                navigateToSetting = { isSettingVisible = true },
            )
        }
    }
}

@OptIn(ExperimentalTime::class)
@Composable
// retention prompt の表示記録と遷移を composition 適用後に dispatch し、画面離脱時はキャンセルする。
@Suppress("UnnecessaryLaunchedEffect")
private fun HandleBillingRetentionPrompt(
    uiState: MainUiState,
    navController: NavHostController,
    isHomeVisible: Boolean,
    onBillingRetentionPromptShow: () -> Unit,
) {
    val currentOnBillingRetentionPromptShown by rememberUpdatedState(onBillingRetentionPromptShow)
    var shownPromptDedupeKey by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(uiState.setting, uiState.isBillingSyncSucceeded, uiState.isAppLocked, isHomeVisible) {
        val currentTimeMillis = Clock.System.now().toEpochMilliseconds()
        val canShowPrompt = uiState.canShowBillingRetentionPrompt(currentTimeMillis, isHomeVisible)
        if (!canShowPrompt) return@LaunchedEffect

        val promptDedupeKey = uiState.setting.billingRetentionPromptDedupeKey
        if (shownPromptDedupeKey == promptDedupeKey) return@LaunchedEffect

        shownPromptDedupeKey = promptDedupeKey
        currentOnBillingRetentionPromptShown()
        navController.navigate(
            Destination.BillingRetentionBottomSheet(
                isAnnualOfferShown = uiState.setting.shouldShowBillingRetentionAnnualOffer,
            ),
        )
    }
}

private fun MainUiState.canShowBillingRetentionPrompt(currentTimeMillis: Long, isHomeVisible: Boolean): Boolean {
    if (!isBillingSyncSucceeded) return false
    if (isAppLocked) return false
    if (!isHomeVisible) return false

    return setting.canShowBillingRetentionPrompt(currentTimeMillis)
}
