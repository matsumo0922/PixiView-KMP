package me.matsumo.fanbox.feature.setting.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable

const val SettingThemeDialogRoute = "SettingTheme"

fun NavController.navigateToSettingTheme() {
    this.navigate(SettingThemeDialogRoute)
}

fun NavGraphBuilder.settingThemeScreen(
    navigateToBillingPlus: () -> Unit,
    terminate: () -> Unit,
) {
    composable(SettingThemeDialogRoute) {
        SettingThemeRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToBillingPlus = navigateToBillingPlus,
            terminate = terminate,
        )
    }
}
