package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.setting_top_general
import me.matsumo.fanbox.core.resources.setting_top_general_app_lock
import me.matsumo.fanbox.core.resources.setting_top_general_app_lock_description
import me.matsumo.fanbox.core.resources.setting_top_general_auto_image_preview
import me.matsumo.fanbox.core.resources.setting_top_general_auto_image_preview_description
import me.matsumo.fanbox.core.resources.setting_top_general_default_follow_tab
import me.matsumo.fanbox.core.resources.setting_top_general_default_follow_tab_description
import me.matsumo.fanbox.core.resources.setting_top_general_grid_mode
import me.matsumo.fanbox.core.resources.setting_top_general_grid_mode_description
import me.matsumo.fanbox.core.resources.setting_top_general_hide_adult_contents
import me.matsumo.fanbox.core.resources.setting_top_general_hide_adult_contents_description
import me.matsumo.fanbox.core.resources.setting_top_general_hide_restricted_contents
import me.matsumo.fanbox.core.resources.setting_top_general_hide_restricted_contents_description
import me.matsumo.fanbox.core.resources.setting_top_general_infinity_post_detail
import me.matsumo.fanbox.core.resources.setting_top_general_infinity_post_detail_description
import me.matsumo.fanbox.core.resources.setting_top_general_override_adult_contents_setting
import me.matsumo.fanbox.core.resources.setting_top_general_override_adult_contents_setting_description
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem

@Composable
internal fun SettingTopGeneralSection(
    setting: Setting,
    onClickAppLock: (Boolean) -> Unit,
    onClickFollowTabDefaultHome: (Boolean) -> Unit,
    onClickHideAdultContents: (Boolean) -> Unit,
    onClickOverrideAdultContents: (Boolean) -> Unit,
    onClickHideRestricted: (Boolean) -> Unit,
    onClickGridMode: (Boolean) -> Unit,
    onClickInfinityPostDetail: (Boolean) -> Unit,
    onClickAutoImagePreview: (Boolean) -> Unit,
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
            value = setting.isUseAppLock,
            onValueChanged = onClickAppLock,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_default_follow_tab,
            description = Res.string.setting_top_general_default_follow_tab_description,
            value = setting.isDefaultFollowTabInHome,
            onValueChanged = onClickFollowTabDefaultHome,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_hide_adult_contents,
            description = Res.string.setting_top_general_hide_adult_contents_description,
            value = setting.isHideAdultContents,
            onValueChanged = onClickHideAdultContents,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_override_adult_contents_setting,
            description = Res.string.setting_top_general_override_adult_contents_setting_description,
            value = setting.isOverrideAdultContents,
            onValueChanged = onClickOverrideAdultContents,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_hide_restricted_contents,
            description = Res.string.setting_top_general_hide_restricted_contents_description,
            value = setting.isHideRestricted,
            onValueChanged = onClickHideRestricted,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_grid_mode,
            description = Res.string.setting_top_general_grid_mode_description,
            value = setting.isUseGridMode,
            onValueChanged = onClickGridMode,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_infinity_post_detail,
            description = Res.string.setting_top_general_infinity_post_detail_description,
            value = setting.isUseInfinityPostDetail,
            onValueChanged = onClickInfinityPostDetail,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_general_auto_image_preview,
            description = Res.string.setting_top_general_auto_image_preview_description,
            value = setting.isAutoImagePreview,
            onValueChanged = onClickAutoImagePreview,
        )
    }
}
