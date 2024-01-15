package me.matsumo.fanbox.feature.setting.developer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import moe.tlaster.precompose.navigation.Navigator
import moe.tlaster.precompose.navigation.RouteBuilder

const val SettingDeveloperRoute = "SettingDeveloper"

fun Navigator.navigateToSettingDeveloper() {
    this.navigate(SettingDeveloperRoute)
}

fun RouteBuilder.settingDeveloperDialog(
    terminate: () -> Unit,
) {
    dialog(
        route = SettingDeveloperRoute,
    ) {
        Dialog(
            onDismissRequest = terminate,
        ) {
            SettingDeveloperDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp)),
                terminate = terminate,
            )
        }
    }
}
