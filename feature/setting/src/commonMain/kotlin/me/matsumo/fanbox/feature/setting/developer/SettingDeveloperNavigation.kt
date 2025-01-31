package me.matsumo.fanbox.feature.setting.developer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import me.matsumo.fanbox.core.model.Destination

fun NavGraphBuilder.settingDeveloperDialog(
    terminate: () -> Unit,
) {
    dialog<Destination.SettingDeveloperDialog> {
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
