package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.SettingTextItem

@Composable
internal fun SettingTopAccountSection(
    onClickAccountSetting: () -> Unit,
    onClickNotifySetting: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = MR.strings.setting_account,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_account_title,
            description = MR.strings.setting_account_description,
            onClick = onClickAccountSetting,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_notify_title,
            description = MR.strings.setting_notify_description,
            onClick = onClickNotifySetting,
        )
    }
}
