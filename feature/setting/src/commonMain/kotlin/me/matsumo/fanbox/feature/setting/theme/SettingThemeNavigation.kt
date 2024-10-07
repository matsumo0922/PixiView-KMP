package me.matsumo.fanbox.feature.setting.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val SettingThemeRoute = "SettingTheme"

fun NavController.navigateToSettingTheme() {
    this.navigateWithLog(SettingThemeRoute)
}

fun NavGraphBuilder.settingThemeScreen(
    navigateToBillingPlus: (String?) -> Unit,
    terminate: () -> Unit,
) {
    composable(SettingThemeRoute) {
        SettingThemeRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToBillingPlus = navigateToBillingPlus,
            terminate = terminate,
        )
    }
}
