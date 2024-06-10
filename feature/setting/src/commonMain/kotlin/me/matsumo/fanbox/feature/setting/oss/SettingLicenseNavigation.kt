package me.matsumo.fanbox.feature.setting.oss

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.animation.NavigateAnimation
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

const val SettingLicenseRoute = "SettingLicense"

fun NavController.navigateToSettingLicense() {
    this.navigateWithLog(SettingLicenseRoute)
}

fun NavGraphBuilder.settingLicenseScreen(
    terminate: () -> Unit,
) {
    composable(SettingLicenseRoute) {
        SettingLicenseScreen(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
