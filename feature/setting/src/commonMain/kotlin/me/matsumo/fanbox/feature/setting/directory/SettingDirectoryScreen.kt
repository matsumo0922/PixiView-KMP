package me.matsumo.fanbox.feature.setting.directory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveScaffold
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.setting_top_file
import me.matsumo.fanbox.feature.setting.SettingTheme
import me.matsumo.fanbox.feature.setting.directory.items.SettingDirectoryContent
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingDirectoryRoute(
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingDirectoryViewModel = koinViewModel(),
    toastExtension: ToastExtension = koinInject(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val snackbarHostState = LocalSnackbarHostState.current
    val scope = rememberCoroutineScope()

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
    ) {
        SettingDirectoryScreen(
            modifier = Modifier.fillMaxSize(),
            imageDirectory = it.imageDirectory,
            fileDirectory = it.fileDirectory,
            postDirectory = it.postDirectory,
            onSaveImageDirectory = viewModel::setImageSaveDirectory,
            onSaveFileDirectory = viewModel::setFileSaveDirectory,
            onSavePostDirectory = viewModel::setPostSaveDirectory,
            onShowSnackbar = { scope.launch { toastExtension.show(snackbarHostState, it) } },
            onTerminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAdaptiveApi::class)
@Composable
private fun SettingDirectoryScreen(
    imageDirectory: String,
    fileDirectory: String,
    postDirectory: String,
    onSaveImageDirectory: (String) -> Unit,
    onSaveFileDirectory: (String) -> Unit,
    onSavePostDirectory: (String) -> Unit,
    onShowSnackbar: (StringResource) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)

    AdaptiveScaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            SettingTheme {
                LargeTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(
                            text = stringResource(Res.string.setting_top_file),
                        )
                    },
                    navigationIcon = {
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .padding(6.dp)
                                .clickable { onTerminate.invoke() },
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    scrollBehavior = behavior,
                )
            }
        },
    ) { paddingValues ->
        SettingDirectoryContent(
            modifier = Modifier.padding(paddingValues),
            imageDirectory = imageDirectory,
            fileDirectory = fileDirectory,
            postDirectory = postDirectory,
            onSaveImageDirectory = onSaveImageDirectory,
            onSaveFileDirectory = onSaveFileDirectory,
            onSavePostDirectory = onSavePostDirectory,
            onShowSnackbar = onShowSnackbar,
            onTerminate = onTerminate,
        )
    }
}
