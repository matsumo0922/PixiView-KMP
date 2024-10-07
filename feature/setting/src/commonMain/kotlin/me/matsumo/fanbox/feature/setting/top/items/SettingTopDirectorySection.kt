package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.component.SettingTextItem
import me.matsumo.fanbox.core.ui.setting_top_file
import me.matsumo.fanbox.core.ui.setting_top_file_directory
import me.matsumo.fanbox.core.ui.setting_top_file_directory_description
import me.matsumo.fanbox.core.ui.setting_top_theme
import me.matsumo.fanbox.core.ui.setting_top_theme_app
import me.matsumo.fanbox.core.ui.setting_top_theme_app_description

@Composable
internal fun SettingTopDirectorySection(
    onClickDirectory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.setting_top_file,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_file_directory,
            description = Res.string.setting_top_file_directory_description,
            onClick = onClickDirectory,
        )
    }
}
