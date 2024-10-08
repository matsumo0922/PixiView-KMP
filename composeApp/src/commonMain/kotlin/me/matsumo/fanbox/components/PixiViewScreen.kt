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
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.MainUiState
import me.matsumo.fanbox.feature.welcome.WelcomeNavHost

@Composable
internal fun PixiViewScreen(
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
            PixiViewContent(
                modifier = Modifier.fillMaxSize(),
                downloadState = uiState.downloadState,
            )
        }
    }
}