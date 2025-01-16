package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import me.matsumo.fanbox.core.model.DownloadFileType
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.setting_top_file
import me.matsumo.fanbox.core.resources.setting_top_file_directory
import me.matsumo.fanbox.core.resources.setting_top_file_directory_description
import me.matsumo.fanbox.core.resources.setting_top_file_type
import me.matsumo.fanbox.core.resources.setting_top_file_type_description
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem
import me.matsumo.fanbox.core.ui.component.SettingTextItem

@Composable
internal fun SettingTopFileSection(
    userData: UserData,
    onClickDirectory: () -> Unit,
    onClickDownloadFileType: (DownloadFileType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        SettingTopTitleItem(
            modifier = Modifier.fillMaxWidth(),
            text = Res.string.setting_top_file,
        )

        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_file_directory,
            description = Res.string.setting_top_file_directory_description,
            onClick = onClickDirectory,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.setting_top_file_type,
            description = Res.string.setting_top_file_type_description,
            value = userData.downloadFileType == DownloadFileType.ORIGINAL,
            onValueChanged = {
                onClickDownloadFileType.invoke(if (it) DownloadFileType.ORIGINAL else DownloadFileType.THUMBNAIL)
            },
        )
    }
}
