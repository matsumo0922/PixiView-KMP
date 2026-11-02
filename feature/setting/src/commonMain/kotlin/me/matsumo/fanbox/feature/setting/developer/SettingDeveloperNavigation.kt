package me.matsumo.fanbox.feature.setting.developer

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.scene.DialogSceneStrategy
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.settingDeveloperDialogEntry(terminate: () -> Unit) {
    entry<Destination.SettingDeveloperDialog>(
        metadata = DialogSceneStrategy.dialog(),
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
