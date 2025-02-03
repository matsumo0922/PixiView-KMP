package me.matsumo.fanbox.feature.setting.translate

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.dialog
import androidx.navigation.toRoute
import me.matsumo.fanbox.core.model.Destination

fun NavGraphBuilder.settingTranslationDialog(
    terminate: () -> Unit,
) {
    dialog<Destination.SettingTranslationDialog> {
        Dialog(
            onDismissRequest = terminate,
        ) {
            SettingTranslationDialog(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight()
                    .clip(RoundedCornerShape(16.dp)),
                defaultLanguage = it.toRoute<Destination.SettingTranslationDialog>().language,
                terminate = terminate,
            )
        }
    }
}
