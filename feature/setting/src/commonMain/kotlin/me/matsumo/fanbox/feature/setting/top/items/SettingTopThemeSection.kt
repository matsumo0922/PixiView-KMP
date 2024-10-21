package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.ui.component.SettingTextItem
import me.matsumo.fanbox.core.resources.setting_top_theme
import me.matsumo.fanbox.core.resources.setting_top_theme_app
import me.matsumo.fanbox.core.resources.setting_top_theme_app_description

@Composable
internal fun SettingTopThemeSection(
    onClickAppTheme: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.setting_top_theme,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_theme_app,
            description = Res.string.setting_top_theme_app_description,
            onClick = onClickAppTheme,
        )
    }
}
