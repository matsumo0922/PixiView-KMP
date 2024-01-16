package me.matsumo.fanbox.feature.setting.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val SettingThemeDialogRoute = "SettingTheme"

fun Navigator.navigateToSettingTheme() {
    this.navigate(SettingThemeDialogRoute)
}

fun RouteBuilder.settingThemeScreen(
    navigateToBillingPlus: () -> Unit,
    terminate: () -> Unit,
) {
    scene(SettingThemeDialogRoute) {
        SettingThemeRoute(
            modifier = Modifier.fillMaxSize(),
            navigateToBillingPlus = navigateToBillingPlus,
            terminate = terminate,
        )
    }
}
