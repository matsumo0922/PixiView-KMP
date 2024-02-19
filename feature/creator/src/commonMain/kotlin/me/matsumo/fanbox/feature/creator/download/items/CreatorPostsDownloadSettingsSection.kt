package me.matsumo.fanbox.feature.creator.download.items

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem

@Composable
internal fun CreatorPostsDownloadSettingsSection(
    ignoreKeyword: String,
    isIgnoreFreePosts: Boolean,
    isIgnoreFiles: Boolean,
    onUpdateIgnoreKeyword: (String) -> Unit,
    onClickIgnoreFreePosts: (Boolean) -> Unit,
    onClickIgnoreFiles: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Text(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            text = stringResource(MR.strings.creator_posts_download_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.creator_posts_download_ignore_free,
            description = MR.strings.creator_posts_download_ignore_free_description,
            value = isIgnoreFreePosts,
            onValueChanged = onClickIgnoreFreePosts,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.creator_posts_download_ignore_file,
            description = MR.strings.creator_posts_download_ignore_file_description,
            value = isIgnoreFiles,
            onValueChanged = onClickIgnoreFiles,
        )

        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 12.dp),
            value = ignoreKeyword,
            onValueChange = onUpdateIgnoreKeyword,
            placeholder = {
                Text(
                    text = stringResource(MR.strings.creator_posts_download_ignore_keyword_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        )
    }
}
