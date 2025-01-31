package me.matsumo.fanbox.feature.setting.oss

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination

fun NavGraphBuilder.settingLicenseScreen(
    terminate: () -> Unit,
) {
    composable<Destination.SettingLicense> {
        SettingLicenseScreen(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
