package me.matsumo.fanbox.feature.setting.translate

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

fun EntryProviderScope<Destination>.settingTranslationDialogEntry(terminate: () -> Unit) {
    entry<Destination.SettingTranslationDialog>(
        metadata = DialogSceneStrategy.dialog(),
    ) { destination ->
        Dialog(
            onDismissRequest = terminate,
        ) {
            SettingTranslationDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp)),
                defaultLanguage = destination.language,
                terminate = terminate,
            )
        }
    }
}
