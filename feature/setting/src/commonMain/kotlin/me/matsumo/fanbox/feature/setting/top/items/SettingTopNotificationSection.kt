package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.setting_top_notification
import me.matsumo.fanbox.core.resources.setting_top_notification_hide_adult_contents
import me.matsumo.fanbox.core.resources.setting_top_notification_hide_adult_contents_description
import me.matsumo.fanbox.core.resources.setting_top_notification_new_post
import me.matsumo.fanbox.core.resources.setting_top_notification_new_post_description
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem

@Composable
internal fun SettingTopNotificationSection(
    setting: Setting,
    onClickNewPostNotification: (Boolean) -> Unit,
    onClickHideAdultContentsInNotification: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.setting_top_notification,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_notification_new_post,
            description = Res.string.setting_top_notification_new_post_description,
            value = setting.isNewPostNotificationEnabled,
            onValueChange = onClickNewPostNotification,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_notification_hide_adult_contents,
            description = Res.string.setting_top_notification_hide_adult_contents_description,
            value = setting.isHideAdultContentsInNotification,
            onValueChange = onClickHideAdultContentsInNotification,
        )
    }
}
