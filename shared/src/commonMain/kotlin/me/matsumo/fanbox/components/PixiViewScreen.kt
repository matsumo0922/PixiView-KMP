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
import me.matsumo.fanbox.MainUiState
import me.matsumo.fanbox.PixiViewSettingNavHost
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.LoginState
import me.matsumo.fanbox.core.model.messageOrNull
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_executed
import me.matsumo.fanbox.core.resources.error_network
import me.matsumo.fanbox.core.resources.login_relogin
import me.matsumo.fanbox.core.resources.login_session_unreadable_message
import me.matsumo.fanbox.core.resources.login_session_unreadable_title
import me.matsumo.fanbox.core.ui.navigation.PixiViewNavigator
import me.matsumo.fanbox.core.ui.navigation.rememberPixiViewNavigator
import me.matsumo.fanbox.core.ui.view.ErrorView
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
    onRequestRelogin: () -> Unit,
    onBillingRetentionPromptShow: () -> Unit,
    onPostDetailClose: suspend () -> Unit,
    modifier: Modifier = Modifier,
) {
    val navigator = rememberPixiViewNavigator(Destination.Library)

    var showPaywallFlag by remember { mutableStateOf(false) }
    var isLibraryHomeVisible by remember { mutableStateOf(false) }
    var isAgreedTeams by remember {
        mutableStateOf(uiState.setting.isAgreedPrivacyPolicy && uiState.setting.isAgreedTermsOfService)
    }
    val isLoggedIn = uiState.loginState is LoginState.LoggedIn
    var isAllowedPermission by remember(uiState.setting, isLoggedIn) { mutableStateOf(true) }

    SideEffect(true) {
        if (uiState.setting.pixiViewId.isBlank()) {
            onRequestInitPixiViewId.invoke()
        }

        if (uiState.setting.firstLaunchTime == -1L) {
            onRequestFirstLaunchFlag.invoke()
        }
    }

    val contentType = pixiViewContentTypeOf(
        loginState = uiState.loginState,
        isAgreedTeams = isAgreedTeams,
        isAllowedPermission = isAllowedPermission,
    )

    AnimatedContent(
        modifier = modifier,
        targetState = contentType,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        label = "pixiViewContentType",
    ) { type ->
        when (type) {
            PixiViewContentType.Welcome -> {
                WelcomeContent(
                    modifier = Modifier.fillMaxSize(),
                    isAgreedTeams = isAgreedTeams,
                    isLoggedIn = isLoggedIn,
                    onComplete = {
                        isAgreedTeams = true
                        isAllowedPermission = true
                        showPaywallFlag = true

                        onRequestUpdateState.invoke()
                    },
                )
            }

            PixiViewContentType.StartupError -> {
                StartupErrorContent(
                    modifier = Modifier.fillMaxSize(),
                    loginState = uiState.loginState,
                    onRetry = onRequestUpdateState,
                    onRelogin = onRequestRelogin,
                )
            }

            PixiViewContentType.Main -> {
                PixiViewContent(
                    modifier = Modifier.fillMaxSize(),
                    navigator = navigator,
                    onPostDetailClose = onPostDetailClose,
                    onLibraryHomeVisibilityChange = { isVisible -> isLibraryHomeVisible = isVisible },
                )

                HandleBillingRetentionPrompt(
                    uiState = uiState,
                    navigator = navigator,
                    isHomeVisible = isLibraryHomeVisible && navigator.currentDestination == Destination.Library,
                    onBillingRetentionPromptShow = onBillingRetentionPromptShow,
                )

                if (showPaywallFlag) {
                    LaunchedEffect(true) {
                        navigator.navigate(Destination.BillingPlusBottomSheet(null))
                        showPaywallFlag = false
                    }
                }
            }
        }
    }
}

/** [PixiViewScreen] が全体に表示する内容の種類。 */
private enum class PixiViewContentType {
    Welcome,
    StartupError,
    Main,
}

// 同意前は welcome を優先する。ログインを確かめられなかったときは、ログイン画面ではなく理由を示す画面を出す。
private fun pixiViewContentTypeOf(
    loginState: LoginState,
    isAgreedTeams: Boolean,
    isAllowedPermission: Boolean,
): PixiViewContentType = when {
    !isAgreedTeams -> PixiViewContentType.Welcome
    loginState is LoginState.Unverified -> PixiViewContentType.StartupError
    loginState is LoginState.SessionUnreadable -> PixiViewContentType.StartupError
    loginState !is LoginState.LoggedIn -> PixiViewContentType.Welcome
    !isAllowedPermission -> PixiViewContentType.Welcome
    else -> PixiViewContentType.Main
}

/**
 * 起動時にログインを確かめられなかった理由を示す。
 *
 * 確かめられなかっただけなら、再試行とログインのし直しを選べる。保存したセッションが読めない場合は、
 * ログインし直すしかないため、その操作だけを出す。
 */
@Composable
private fun StartupErrorContent(
    loginState: LoginState,
    onRetry: () -> Unit,
    onRelogin: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (loginState is LoginState.Unverified) {
        ErrorView(
            modifier = modifier,
            title = Res.string.error_executed,
            message = loginState.errorKind.messageOrNull() ?: Res.string.error_network,
            retryAction = onRetry,
            secondaryActionTitle = Res.string.login_relogin,
            secondaryAction = onRelogin,
        )
    } else {
        ErrorView(
            modifier = modifier,
            title = Res.string.login_session_unreadable_title,
            message = Res.string.login_session_unreadable_message,
            serviceStatus = false,
            retryTitle = Res.string.login_relogin,
            retryAction = onRelogin,
        )
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
    navigator: PixiViewNavigator,
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
        navigator.navigate(
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
