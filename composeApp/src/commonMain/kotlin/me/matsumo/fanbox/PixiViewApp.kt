package me.matsumo.fanbox

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.component.PixiViewBackground
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.NavigationType
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.theme.DarkDefaultColorScheme
import me.matsumo.fanbox.core.ui.theme.LightDefaultColorScheme
import me.matsumo.fanbox.core.ui.theme.PixiViewTheme
import me.matsumo.fanbox.feature.welcome.WelcomeNavHost
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel

@Composable
fun PixiViewApp(
    windowSize: WindowWidthSizeClass,
    modifier: Modifier = Modifier,
    viewModel: PixiViewViewModel = koinViewModel(PixiViewViewModel::class)
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val shouldUseDarkTheme = shouldUseDarkTheme(screenState)
    val shouldUseDynamicColor = shouldUseDynamicColor(screenState)

    val navigationType = when (windowSize) {
        WindowWidthSizeClass.Medium -> PixiViewNavigationType.NavigationRail
        WindowWidthSizeClass.Expanded -> PixiViewNavigationType.PermanentNavigationDrawer
        else -> PixiViewNavigationType.BottomNavigation
    }

    CompositionLocalProvider(LocalNavigationType provides NavigationType(navigationType)) {
        PixiViewTheme {
            PixiViewBackground(modifier) {
                AsyncLoadContents(
                    modifier = Modifier.fillMaxSize(),
                    screenState = screenState,
                    containerColor = if (shouldUseDarkTheme) DarkDefaultColorScheme.surface else LightDefaultColorScheme.surface,
                ) {
                    PixiViewScreen(
                        modifier = Modifier.fillMaxSize(),
                        uiState = it,
                        onRequestInitPixiViewId = viewModel::initPixiViewId,
                        onRequestUpdateState = viewModel::updateState,
                    )
                }
            }
        }
    }
}

@Composable
private fun PixiViewScreen(
    uiState: MainUiState,
    onRequestInitPixiViewId: () -> Unit,
    onRequestUpdateState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isAgreedTeams by remember(uiState.userData) { mutableStateOf(uiState.userData.isAgreedPrivacyPolicy && uiState.userData.isAgreedTermsOfService) }
    var isAllowedPermission by remember(uiState.userData, uiState.isLoggedIn) { mutableStateOf(true) }

    LaunchedEffect(true) {
        if (uiState.userData.pixiViewId.isBlank()) {
            onRequestInitPixiViewId.invoke()
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
                isAllowedPermission = isAllowedPermission,
                isLoggedIn = uiState.isLoggedIn,
                onComplete = {
                    isAgreedTeams = true
                    isAllowedPermission = true

                    onRequestUpdateState.invoke()
                }
            )
        } else {
            IdleScreen(
                modifier = Modifier.fillMaxSize(),
                navigationType = LocalNavigationType.current.type,
            )
        }
    }
}

@Composable
private fun IdleScreen(
    navigationType: PixiViewNavigationType,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Text(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(),
            text = "Hello, World!",
        )
    }
}

@Composable
private fun shouldUseDarkTheme(screenState: ScreenState<MainUiState>): Boolean {
    val default = isSystemInDarkTheme()
    val data = (screenState as? ScreenState.Idle)?.data ?: return default

    return when (data.userData.themeConfig) {
        ThemeConfig.Light -> false
        ThemeConfig.Dark -> true
        ThemeConfig.System -> default
    }
}

private fun shouldUseDynamicColor(screenState: ScreenState<MainUiState>): Boolean {
    val data = (screenState as? ScreenState.Idle)?.data ?: return false
    return data.userData.isDynamicColor
}
