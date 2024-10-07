package me.matsumo.fanbox.feature.setting.directory.items

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.error_developing_feature
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource

@Composable
internal actual fun SettingDirectoryContent(
    imageDirectory: String,
    fileDirectory: String,
    postDirectory: String,
    onSaveImageDirectory: (String) -> Unit,
    onSaveFileDirectory: (String) -> Unit,
    onSavePostDirectory: (String) -> Unit,
    onShowSnackbar: (StringResource) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier
) {
    Box(
        modifier = modifier.padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(Res.string.error_developing_feature),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground,
        )
    }
}
