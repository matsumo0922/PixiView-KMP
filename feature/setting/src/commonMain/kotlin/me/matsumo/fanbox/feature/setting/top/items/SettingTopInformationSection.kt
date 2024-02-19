package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.SettingTextItem

@Composable
internal fun SettingTopInformationSection(
    userData: UserData,
    fanboxMetaData: FanboxMetaData,
    fanboxSessionId: String,
    config: PixiViewConfig,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current

    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = MR.strings.setting_top_information,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(MR.strings.setting_top_information_version),
            description = "${config.versionName}:${config.versionCode}" + when {
                userData.isPlusMode && userData.isDeveloperMode -> " [P+D]"
                userData.isPlusMode -> " [Premium]"
                userData.isDeveloperMode -> " [Developer]"
                else -> ""
            } + if (userData.isTestUser) " [Test]" else "",
            onClick = { /* do nothing */ },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(MR.strings.setting_top_information_id),
            description = userData.pixiViewId,
            onLongClick = { clipboard.setText(AnnotatedString(userData.pixiViewId)) },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(MR.strings.setting_top_information_fanbox_session_id),
            description = fanboxSessionId,
            onLongClick = { clipboard.setText(AnnotatedString(fanboxSessionId)) },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(MR.strings.setting_top_information_csrf_token),
            description = fanboxMetaData.csrfToken,
            onLongClick = { clipboard.setText(AnnotatedString(fanboxMetaData.csrfToken)) },
        )
    }
}
