package me.matsumo.fanbox.feature.setting.directory.items

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.net.toUri
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.ui.component.SettingTextItem
import me.matsumo.fanbox.core.resources.setting_top_file_directory_file
import me.matsumo.fanbox.core.resources.setting_top_file_directory_image
import me.matsumo.fanbox.core.resources.setting_top_file_directory_post
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
    modifier: Modifier,
) {
    val context = LocalContext.current
    val imageDirectoryUri = remember(imageDirectory) { imageDirectory.toUri() }
    val fileDirectoryUri = remember(fileDirectory) { fileDirectory.toUri() }
    val postDirectoryUri = remember(postDirectory) { postDirectory.toUri() }

    val imageDirectoryPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        val uri = it ?: return@rememberLauncherForActivityResult
        val modeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        context.contentResolver.takePersistableUriPermission(uri, modeFlags)
        onSaveImageDirectory.invoke(uri.toString())
    }

    val fileDirectoryPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        val uri = it ?: return@rememberLauncherForActivityResult
        val modeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        context.contentResolver.takePersistableUriPermission(uri, modeFlags)
        onSaveFileDirectory.invoke(uri.toString())
    }

    val postDirectoryPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) {
        val uri = it ?: return@rememberLauncherForActivityResult
        val modeFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION

        context.contentResolver.takePersistableUriPermission(uri, modeFlags)
        onSavePostDirectory.invoke(uri.toString())
    }

    LazyColumn(modifier) {
        item {
            SettingTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.setting_top_file_directory_image),
                description = imageDirectoryUri.path.ifNullOrBlank("Unknown"),
                onClick = { imageDirectoryPicker.launch(imageDirectoryUri) },
            )
        }

        item {
            SettingTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.setting_top_file_directory_file),
                description = fileDirectoryUri.path.ifNullOrBlank("Unknown"),
                onClick = { fileDirectoryPicker.launch(fileDirectoryUri) },
            )
        }

        item {
            SettingTextItem(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.setting_top_file_directory_post),
                description = postDirectoryUri.path.ifNullOrBlank("Unknown"),
                onClick = { postDirectoryPicker.launch(fileDirectoryUri) },
            )
        }
    }
}

private fun String?.ifNullOrBlank(default: String): String {
    return if (this.isNullOrBlank()) default else this
}
