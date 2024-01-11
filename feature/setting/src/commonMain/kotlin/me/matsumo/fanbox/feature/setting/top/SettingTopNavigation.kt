package me.matsumo.fanbox.feature.setting.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val SettingTopRoute = "settingTop"

fun Navigator.navigateToSettingTop() {
    this.navigate(SettingTopRoute)
}

fun RouteBuilder.settingTopScreen(
    navigateToThemeSetting: () -> Unit,
    navigateToBillingPlus: () -> Unit,
    navigateToLogoutDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    navigateToOpenSourceLicense: () -> Unit,
    navigateToSettingDeveloper: () -> Unit,
    terminate: () -> Unit,
) {
    scene(
        route = SettingTopRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        SettingTopRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToThemeSetting = navigateToThemeSetting,
            navigateToBillingPlus = navigateToBillingPlus,
            navigateToOpenSourceLicense = navigateToOpenSourceLicense,
            navigateToLogoutDialog = navigateToLogoutDialog,
            navigateToSettingDeveloper = navigateToSettingDeveloper,
            terminate = terminate,
        )
    }
}
