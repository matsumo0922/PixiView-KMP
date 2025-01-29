package me.matsumo.fanbox.feature.setting.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog
import me.matsumo.fanbox.core.model.SimpleAlertContents

fun NavGraphBuilder.settingTopScreen(
    navigateToThemeSetting: () -> Unit,
    navigateToDirectorySetting: () -> Unit,
    navigateToBillingPlus: (String?) -> Unit,
    navigateToLogoutDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    navigateToOpenSourceLicense: () -> Unit,
    navigateToSettingDeveloper: () -> Unit,
    terminate: () -> Unit,
) {
    composable<Destination.SettingTop> {
        SettingTopRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToThemeSetting = navigateToThemeSetting,
            navigateToDirectorySetting = navigateToDirectorySetting,
            navigateToBillingPlus = navigateToBillingPlus,
            navigateToOpenSourceLicense = navigateToOpenSourceLicense,
            navigateToLogoutDialog = navigateToLogoutDialog,
            navigateToSettingDeveloper = navigateToSettingDeveloper,
            terminate = terminate,
        )
    }
}
