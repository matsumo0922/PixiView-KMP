package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem
import me.matsumo.fanbox.core.ui.component.SettingTextItem
import me.matsumo.fanbox.core.resources.setting_top_information_privacy_policy
import me.matsumo.fanbox.core.resources.setting_top_information_team_of_service
import me.matsumo.fanbox.core.resources.setting_top_others
import me.matsumo.fanbox.core.resources.setting_top_others_developer_mode
import me.matsumo.fanbox.core.resources.setting_top_others_developer_mode_description
import me.matsumo.fanbox.core.resources.setting_top_others_logout
import me.matsumo.fanbox.core.resources.setting_top_others_logout_description
import me.matsumo.fanbox.core.resources.setting_top_others_open_source_license
import me.matsumo.fanbox.core.resources.setting_top_others_open_source_license_description
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingTopOthersSection(
    userData: UserData,
    onClickTeamsOfService: () -> Unit,
    onClickPrivacyPolicy: () -> Unit,
    onClickLogout: () -> Unit,
    onClickOpenSourceLicense: () -> Unit,
    onClickDeveloperMode: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.setting_top_others,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_team_of_service),
            onClick = onClickTeamsOfService,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_privacy_policy),
            onClick = onClickPrivacyPolicy,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_others_logout,
            description = Res.string.setting_top_others_logout_description,
            onClick = onClickLogout,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_others_open_source_license,
            description = Res.string.setting_top_others_open_source_license_description,
            onClick = { onClickOpenSourceLicense.invoke() },
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_others_developer_mode,
            description = Res.string.setting_top_others_developer_mode_description,
            value = userData.isDeveloperMode,
            onValueChanged = onClickDeveloperMode,
        )
    }
}
