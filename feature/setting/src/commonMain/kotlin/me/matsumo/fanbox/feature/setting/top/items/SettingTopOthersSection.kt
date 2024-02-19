package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem
import me.matsumo.fanbox.core.ui.component.SettingTextItem

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
            text = MR.strings.setting_top_others,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(MR.strings.setting_top_information_team_of_service),
            onClick = onClickTeamsOfService,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(MR.strings.setting_top_information_privacy_policy),
            onClick = onClickPrivacyPolicy,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_others_logout,
            description = MR.strings.setting_top_others_logout_description,
            onClick = onClickLogout,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_others_open_source_license,
            description = MR.strings.setting_top_others_open_source_license_description,
            onClick = { onClickOpenSourceLicense.invoke() },
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_top_others_developer_mode,
            description = MR.strings.setting_top_others_developer_mode_description,
            value = userData.isDeveloperMode,
            onValueChanged = onClickDeveloperMode,
        )
    }
}
