package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.SettingTextItem

@Composable
internal fun SettingTopThemeSection(
    onClickAppTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = MR.strings.setting_top_theme,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_theme_app,
            description = MR.strings.setting_top_theme_app_description,
            onClick = onClickAppTheme,
        )
    }
}
