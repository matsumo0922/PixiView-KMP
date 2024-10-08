package me.matsumo.fanbox.feature.setting.directory.items

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.StringResource

@Composable
internal expect fun SettingDirectoryContent(
    imageDirectory: String,
    fileDirectory: String,
    postDirectory: String,
    onSaveImageDirectory: (String) -> Unit,
    onSaveFileDirectory: (String) -> Unit,
    onSavePostDirectory: (String) -> Unit,
    onShowSnackbar: (StringResource) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
)
