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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import me.matsumo.fanbox.MainUiState
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.component.sheet.rememberBottomSheetNavigator
import me.matsumo.fanbox.feature.welcome.WelcomeNavHost

@Composable
internal fun PixiViewScreen(
    uiState: MainUiState,
    onRequestInitPixiViewId: () -> Unit,
    onRequestUpdateState: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val scope = rememberCoroutineScope()

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
