package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem

@Composable
internal fun SettingTopGeneralSection(
    userData: UserData,
    onClickAppLock: (Boolean) -> Unit,
    onClickFollowTabDefaultHome: (Boolean) -> Unit,
    onClickHideAdultContents: (Boolean) -> Unit,
    onClickOverrideAdultContents: (Boolean) -> Unit,
    onClickHideRestricted: (Boolean) -> Unit,
    onClickGridMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = MR.strings.setting_top_general,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_general_app_lock,
            description = MR.strings.setting_top_general_app_lock_description,
            value = userData.isAppLock,
            onValueChanged = onClickAppLock,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_general_default_follow_tab,
            description = MR.strings.setting_top_general_default_follow_tab_description,
            value = userData.isFollowTabDefaultHome,
            onValueChanged = onClickFollowTabDefaultHome,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_general_hide_adult_contents,
            description = MR.strings.setting_top_general_hide_adult_contents_description,
            value = userData.isHideAdultContents,
            onValueChanged = onClickHideAdultContents,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_general_override_adult_contents_setting,
            description = MR.strings.setting_top_general_override_adult_contents_setting_description,
            value = userData.isOverrideAdultContents,
            onValueChanged = onClickOverrideAdultContents,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_general_hide_restricted_contents,
            description = MR.strings.setting_top_general_hide_restricted_contents_description,
            value = userData.isHideRestricted,
            onValueChanged = onClickHideRestricted,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_general_grid_mode,
            description = MR.strings.setting_top_general_grid_mode_description,
            value = userData.isGridMode,
            onValueChanged = onClickGridMode,
        )
    }
}
