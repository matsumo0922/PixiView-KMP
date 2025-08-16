package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.setting_top_information
import me.matsumo.fanbox.core.resources.setting_top_information_csrf_token
import me.matsumo.fanbox.core.resources.setting_top_information_fanbox_session_id
import me.matsumo.fanbox.core.resources.setting_top_information_id
import me.matsumo.fanbox.core.resources.setting_top_information_version
import me.matsumo.fanbox.core.ui.component.SettingTextItem
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun SettingTopInformationSection(
    setting: Setting,
    fanboxMetaData: FanboxMetaData,
    fanboxSessionId: String,
    config: PixiViewConfig,
    modifier: Modifier = Modifier,
) {
    val clipboard = LocalClipboardManager.current

    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.setting_top_information,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_version),
            description = "${config.versionName}:${config.versionCode}" + when {
                setting.isPlusMode && setting.isDeveloperMode -> " [P+D]"
                setting.isPlusMode -> " [Premium]"
                setting.isDeveloperMode -> " [Developer]"
                else -> ""
            } + if (setting.isTestUser) " [Test]" else "",
            onClick = { /* do nothing */ },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_id),
            description = setting.pixiViewId,
            onLongClick = { clipboard.setText(AnnotatedString(setting.pixiViewId)) },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_fanbox_session_id),
            description = fanboxSessionId,
            onLongClick = { clipboard.setText(AnnotatedString(fanboxSessionId)) },
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = stringResource(Res.string.setting_top_information_csrf_token),
            description = fanboxMetaData.csrfToken,
            onLongClick = { clipboard.setText(AnnotatedString(fanboxMetaData.csrfToken)) },
        )
    }
}
