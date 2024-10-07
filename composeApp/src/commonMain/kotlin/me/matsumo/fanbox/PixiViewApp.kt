package me.matsumo.fanbox

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.icerock.moko.biometry.compose.BindBiometryAuthenticatorEffect
import dev.icerock.moko.biometry.compose.rememberBiometryAuthenticatorFactory
import io.github.aakira.napier.Napier
import kotlinx.collections.immutable.ImmutableMap
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.component.PixiViewBackground
import me.matsumo.fanbox.core.ui.extensition.LocalNavigationType
import me.matsumo.fanbox.core.ui.extensition.NavigationType
import me.matsumo.fanbox.core.ui.extensition.NavigatorExtension
import me.matsumo.fanbox.core.ui.extensition.PixiViewNavigationType
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.theme.DarkDefaultColorScheme
import me.matsumo.fanbox.core.ui.theme.LightDefaultColorScheme
import me.matsumo.fanbox.core.ui.theme.PixiViewTheme
import me.matsumo.fanbox.core.ui.view.LoadingView
import me.matsumo.fanbox.core.ui.view.NativeView
import me.matsumo.fanbox.feature.welcome.WelcomeNavHost
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun PixiViewApp(
    windowSize: WindowWidthSizeClass,
    nativeViews: ImmutableMap<String, () -> NativeView?>,
    modifier: Modifier = Modifier,
    viewModel: PixiViewViewModel = koinViewModel(),
    navigatorExtension: NavigatorExtension = koinInject(),
    pixiViewConfig: PixiViewConfig = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val shouldUseDarkTheme = shouldUseDarkTheme(screenState)

    val biometryAuthenticatorFactory = rememberBiometryAuthenticatorFactory()
    val biometryAuthenticator = biometryAuthenticatorFactory.createBiometryAuthenticator()

    val navigationType = when (windowSize) {
        WindowWidthSizeClass.Medium -> PixiViewNavigationType.NavigationRail
        WindowWidthSizeClass.Expanded -> PixiViewNavigationType.PermanentNavigationDrawer
        else -> PixiViewNavigationType.BottomNavigation
    }

    BindBiometryAuthenticatorEffect(biometryAuthenticator)

    CompositionLocalProvider(LocalNavigationType provides NavigationType(navigationType)) {
        AsyncLoadContents(
            modifier = Modifier.fillMaxSize(),
            screenState = screenState,
            containerColor = if (shouldUseDarkTheme) DarkDefaultColorScheme.surface else LightDefaultColorScheme.surface,
        ) {
            PixiViewTheme(
                fanboxCookie = it.fanboxCookie,
                fanboxMetadata = it.fanboxMetadata,
                themeConfig = it.userData.themeConfig,
                themeColorConfig = it.userData.themeColorConfig,
                pixiViewConfig = pixiViewConfig,
                nativeViews = nativeViews,
            ) {
                PixiViewBackground(modifier) {
                    PixiViewScreen(
                        modifier = Modifier.fillMaxSize(),
                        uiState = it,
                        onRequestInitPixiViewId = viewModel::initPixiViewId,
                        onRequestUpdateState = viewModel::updateState,
                    )

                    AnimatedVisibility(
                        visible = it.isAppLocked,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        LoadingView(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surface),
                        )
                    }

                    if (it.isAppLocked) {
                        scope.launch {
                            if (currentPlatform == Platform.Android) {
                                // Wait for the bind fragment manager.
                                delay(1000)
                            }

                            if (viewModel.tryToAuthenticate(biometryAuthenticator)) {
                                viewModel.setAppLock(false)
                            } else {
                                navigatorExtension.killApp()
                            }
                        }
                    }
                }
            }

            DisposableEffect(lifecycleOwner) {
                val observer = object : DefaultLifecycleObserver {
                    override fun onResume(owner: LifecycleOwner) {
                        Napier.d { "onResume" }

                        viewModel.setAppLock(true)
                        viewModel.billingClientUpdate()
                    }
                }

                lifecycleOwner.lifecycle.addObserver(observer)

                onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
            }
        }
    }

    LaunchedEffect(true) {
        viewModel.billingClientInitialize()
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

    Napier.d { "isShowWelcomeScreen: ${uiState.isLoggedIn}, $isAgreedTeams, $isAllowedPermission" }

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

                    onRequestUpdateState.invoke()
                },
            )
        } else {
            PixiViewNavHost(
                modifier = Modifier.fillMaxSize(),
            )
        }
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
