package me.matsumo.fanbox.feature.setting.directory

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.extensition.navigateWithLog

fun NavGraphBuilder.settingDirectoryScreen(
    terminate: () -> Unit,
) {
    composable<Destination.SettingDirectory> {
        SettingDirectoryRoute(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
