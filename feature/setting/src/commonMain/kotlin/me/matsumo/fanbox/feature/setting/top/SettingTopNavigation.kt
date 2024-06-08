package me.matsumo.fanbox.feature.setting.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SettingTopRoute = "settingTop"

fun NavController.navigateToSettingTop() {
    this.navigate(SettingTopRoute)
}

fun NavGraphBuilder.settingTopScreen(
    navigateToThemeSetting: () -> Unit,
    navigateToBillingPlus: () -> Unit,
    navigateToLogoutDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    navigateToOpenSourceLicense: () -> Unit,
    navigateToSettingDeveloper: () -> Unit,
    terminate: () -> Unit,
) {
    composable(SettingTopRoute) {
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
