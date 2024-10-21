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
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.creator_posts_download_ignore_file
import me.matsumo.fanbox.core.resources.creator_posts_download_ignore_file_description
import me.matsumo.fanbox.core.resources.creator_posts_download_ignore_free
import me.matsumo.fanbox.core.resources.creator_posts_download_ignore_free_description
import me.matsumo.fanbox.core.resources.creator_posts_download_ignore_keyword_placeholder
import me.matsumo.fanbox.core.resources.creator_posts_download_message
import me.matsumo.fanbox.core.ui.component.SettingSwitchItem
import org.jetbrains.compose.resources.stringResource

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
            text = stringResource(Res.string.creator_posts_download_message),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.creator_posts_download_ignore_free,
            description = Res.string.creator_posts_download_ignore_free_description,
            value = isIgnoreFreePosts,
            onValueChanged = onClickIgnoreFreePosts,
        )

        SettingSwitchItem(
            modifier = Modifier.fillMaxWidth(),
            title = Res.string.creator_posts_download_ignore_file,
            description = Res.string.creator_posts_download_ignore_file_description,
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
                    text = stringResource(Res.string.creator_posts_download_ignore_keyword_placeholder),
                    style = MaterialTheme.typography.bodyMedium,
                )
            },
        )
    }
}
