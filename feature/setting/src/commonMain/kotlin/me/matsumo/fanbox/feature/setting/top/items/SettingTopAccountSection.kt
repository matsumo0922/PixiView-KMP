package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.setting_account
import me.matsumo.fanbox.core.resources.setting_account_description
import me.matsumo.fanbox.core.resources.setting_account_title
import me.matsumo.fanbox.core.resources.setting_notify_description
import me.matsumo.fanbox.core.resources.setting_notify_title
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
            text = Res.string.setting_account,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_account_title,
            description = Res.string.setting_account_description,
            onClick = onClickAccountSetting,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_notify_title,
            description = Res.string.setting_notify_description,
            onClick = onClickNotifySetting,
        )
    }
}
