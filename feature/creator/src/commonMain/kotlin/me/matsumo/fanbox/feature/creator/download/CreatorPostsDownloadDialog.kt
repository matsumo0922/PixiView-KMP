package me.matsumo.fanbox.feature.creator.download

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.SnackbarExtension
import me.matsumo.fanbox.core.ui.theme.bold
import me.matsumo.fanbox.core.ui.theme.center
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel
import org.koin.compose.koinInject

@Composable
internal fun CreatorPostsDownloadRoute(
    creatorId: CreatorId,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatorPostsDownloadViewModel = koinViewModel(CreatorPostsDownloadViewModel::class),
    snackbarExtension: SnackbarExtension = koinInject(),
) {
    // val activity = context.getActivity() as PostDownloader
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    LaunchedEffect(creatorId) {
        if (!uiState.isReady) {
            viewModel.fetch(creatorId)
        }
    }

    CreatorPostsDownloadScreen(
        modifier = modifier,
        isReady = uiState.isReady,
        onClickDownload = { isIgnoreFree, isIgnoreFile ->
            /*activity.onDownloadPosts(uiState.posts, isIgnoreFree, isIgnoreFile)*/
            scope.launch { snackbarExtension.showSnackbar(snackbarHostState, MR.strings.creator_posts_download_start) }
            terminate.invoke()
        },
        onTerminate = terminate,
    )
}

@Composable
private fun CreatorPostsDownloadScreen(
    isReady: Boolean,
    onClickDownload: (Boolean, Boolean) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var isIgnoreFree by remember { mutableStateOf(false) }
    var isIgnoreFile by remember { mutableStateOf(false) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(MR.strings.creator_posts_download_title),
            style = MaterialTheme.typography.titleMedium.bold(),
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(MR.strings.creator_posts_download_message),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            SwitchItem(
                modifier = Modifier.fillMaxWidth(),
                title = MR.strings.creator_posts_download_ignore_free,
                value = isIgnoreFree,
                onValueChanged = { isIgnoreFree = it },
            )

            SwitchItem(
                modifier = Modifier.fillMaxWidth(),
                title = MR.strings.creator_posts_download_ignore_file,
                value = isIgnoreFile,
                onValueChanged = { isIgnoreFile = it },
            )
        }

        if (!isReady) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(MR.strings.creator_posts_download_prepare),
                style = MaterialTheme.typography.bodySmall.center(),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp),
                onClick = { onTerminate.invoke() },
            ) {
                Text(text = stringResource(MR.strings.common_cancel))
            }

            Button(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp),
                onClick = { onClickDownload.invoke(isIgnoreFree, isIgnoreFile) },
                contentPadding = PaddingValues(0.dp),
                enabled = isReady,
            ) {
                Text(text = stringResource(MR.strings.common_download))
            }
        }
    }
}

@Composable
private fun SwitchItem(
    title: StringResource,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(title),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
        }

        Switch(
            checked = value,
            onCheckedChange = { onValueChanged.invoke(it) },
        )
    }
}
