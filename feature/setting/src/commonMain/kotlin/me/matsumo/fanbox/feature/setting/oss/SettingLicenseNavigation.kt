package me.matsumo.fanbox.feature.setting.oss

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val SettingLicenseRoute = "SettingLicense"

fun Navigator.navigateToSettingLicense() {
    this.navigate(SettingLicenseRoute)
}

fun RouteBuilder.settingLicenseScreen(
    terminate: () -> Unit,
) {
    scene(
        route = SettingLicenseRoute,
        navTransition = NavigateAnimation.Horizontal.transition,
    ) {
        SettingLicenseScreen(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
