package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem
import me.matsumo.fanbox.core.ui.setting_top_general
import me.matsumo.fanbox.core.ui.setting_top_general_app_lock
import me.matsumo.fanbox.core.ui.setting_top_general_app_lock_description
import me.matsumo.fanbox.core.ui.setting_top_general_default_follow_tab
import me.matsumo.fanbox.core.ui.setting_top_general_default_follow_tab_description
import me.matsumo.fanbox.core.ui.setting_top_general_grid_mode
import me.matsumo.fanbox.core.ui.setting_top_general_grid_mode_description
import me.matsumo.fanbox.core.ui.setting_top_general_hide_adult_contents
import me.matsumo.fanbox.core.ui.setting_top_general_hide_adult_contents_description
import me.matsumo.fanbox.core.ui.setting_top_general_hide_restricted_contents
import me.matsumo.fanbox.core.ui.setting_top_general_hide_restricted_contents_description
import me.matsumo.fanbox.core.ui.setting_top_general_infinity_post_detail
import me.matsumo.fanbox.core.ui.setting_top_general_infinity_post_detail_description
import me.matsumo.fanbox.core.ui.setting_top_general_override_adult_contents_setting
import me.matsumo.fanbox.core.ui.setting_top_general_override_adult_contents_setting_description

@Composable
internal fun SettingTopGeneralSection(
    userData: UserData,
    onClickAppLock: (Boolean) -> Unit,
    onClickFollowTabDefaultHome: (Boolean) -> Unit,
    onClickHideAdultContents: (Boolean) -> Unit,
    onClickOverrideAdultContents: (Boolean) -> Unit,
    onClickHideRestricted: (Boolean) -> Unit,
    onClickGridMode: (Boolean) -> Unit,
    onClickInfinityPostDetail: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.setting_top_general,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_app_lock,
            description = Res.string.setting_top_general_app_lock_description,
            value = userData.isUseAppLock,
            onValueChanged = onClickAppLock,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_default_follow_tab,
            description = Res.string.setting_top_general_default_follow_tab_description,
            value = userData.isDefaultFollowTabInHome,
            onValueChanged = onClickFollowTabDefaultHome,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_hide_adult_contents,
            description = Res.string.setting_top_general_hide_adult_contents_description,
            value = userData.isHideAdultContents,
            onValueChanged = onClickHideAdultContents,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_override_adult_contents_setting,
            description = Res.string.setting_top_general_override_adult_contents_setting_description,
            value = userData.isOverrideAdultContents,
            onValueChanged = onClickOverrideAdultContents,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_hide_restricted_contents,
            description = Res.string.setting_top_general_hide_restricted_contents_description,
            value = userData.isHideRestricted,
            onValueChanged = onClickHideRestricted,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_grid_mode,
            description = Res.string.setting_top_general_grid_mode_description,
            value = userData.isUseGridMode,
            onValueChanged = onClickGridMode,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_infinity_post_detail,
            description = Res.string.setting_top_general_infinity_post_detail_description,
            value = userData.isUseInfinityPostDetail,
            onValueChanged = onClickInfinityPostDetail,
        )
    }
}
