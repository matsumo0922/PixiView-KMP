package me.matsumo.fanbox.feature.creator.download

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.creator_posts_download_button
import me.matsumo.fanbox.core.resources.creator_posts_download_dialog_title
import me.matsumo.fanbox.core.resources.creator_posts_download_title
import me.matsumo.fanbox.core.resources.queue_added
import me.matsumo.fanbox.core.resources.queue_added_action
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.view.LoadingDialog
import me.matsumo.fanbox.feature.creator.download.items.CreatorPostsDownloadItem
import me.matsumo.fanbox.feature.creator.download.items.CreatorPostsDownloadSettingsSection
import me.matsumo.fanbox.feature.creator.download.items.CreatorPostsDownloadUserSection
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CreatorPostsDownloadRoute(
    navigateTo: (Destination) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatorPostsDownloadViewModel = koinViewModel(),
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    AsyncLoadContents(
        modifier = modifier,
        otherModifier = Modifier.fillMaxSize(),
        screenState = screenState,
        retryAction = viewModel::fetch,
        terminate = terminate,
    ) { uiState ->
        CreatorPostsDownloadScreen(
            modifier = Modifier.fillMaxSize(),
            creatorDetail = uiState.creatorDetail,
            posts = uiState.targetPosts.toImmutableList(),
            isIgnoreFreePosts = uiState.isIgnoreFreePosts,
            isIgnoreFiles = uiState.isIgnoreFiles,
            isPrepared = uiState.isPrepared,
            ignoreKeyword = uiState.ignoreKeyword,
            onUpdateIgnoreKeyword = viewModel::updateIgnoreKeyword,
            onClickIgnoreFreePosts = viewModel::updateIgnoreFreePosts,
            onClickIgnoreFiles = viewModel::updateIgnoreFiles,
            onClickDownload = viewModel::download,
            onClickCheckQueue = { navigateTo(Destination.DownloadQueue) },
            terminate = terminate,
        )

        if (!uiState.isPrepared) {
            var progress by remember { mutableStateOf(0f) }
            val progressAnimation by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(300),
            )

            LaunchedEffect(true) {
                viewModel.fetchPosts(
                    paginate = uiState.postsPaginate,
                    updateCallback = { progress = it },
                )
            }

            LoadingDialog(
                progress = progressAnimation,
                text = Res.string.creator_posts_download_dialog_title,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CreatorPostsDownloadScreen(
    creatorDetail: FanboxCreatorDetail,
    posts: ImmutableList<CreatorPostsDownloadData>,
    isIgnoreFreePosts: Boolean,
    isIgnoreFiles: Boolean,
    isPrepared: Boolean,
    ignoreKeyword: String,
    onUpdateIgnoreKeyword: (String) -> Unit,
    onClickIgnoreFreePosts: (Boolean) -> Unit,
    onClickIgnoreFiles: (Boolean) -> Unit,
    onClickDownload: () -> Unit,
    onClickCheckQueue: () -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    snackExtension: ToastExtension = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val state = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.creator_posts_download_title),
                onClickNavigation = terminate,
                scrollBehavior = scrollBehavior,
            )
        },
        bottomBar = {
            HorizontalDivider()

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .navigationBarsPadding()
                    .padding(16.dp),
                onClick = {
                    scope.launch {
                        onClickDownload.invoke()
                        snackExtension.show(
                            snackbarHostState = snackbarHostState,
                            message = Res.string.queue_added,
                            label = Res.string.queue_added_action,
                            callback = {
                                if (it == SnackbarResult.ActionPerformed) {
                                    onClickCheckQueue.invoke()
                                }
                            },
                        )
                    }
                },
                contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                enabled = isPrepared,
            ) {
                Icon(
                    modifier = Modifier.size(18.dp),
                    imageVector = Icons.Default.Download,
                    contentDescription = null,
                )

                Text(
                    modifier = Modifier.padding(start = 8.dp),
                    text = stringResource(Res.string.creator_posts_download_button, posts.size),
                )
            }
        },
        snackbarHost = {
            SnackbarHost(
                hostState = snackbarHostState,
            )
        },
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
            state = state,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item("header") {
                Column(
                    modifier = Modifier
                        .animateContentSize()
                        .fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    CreatorPostsDownloadUserSection(
                        modifier = Modifier.fillMaxWidth(),
                        creatorDetail = creatorDetail,
                    )

                    CreatorPostsDownloadSettingsSection(
                        modifier = Modifier.fillMaxWidth(),
                        ignoreKeyword = ignoreKeyword,
                        isIgnoreFreePosts = isIgnoreFreePosts,
                        isIgnoreFiles = isIgnoreFiles,
                        onUpdateIgnoreKeyword = onUpdateIgnoreKeyword,
                        onClickIgnoreFreePosts = onClickIgnoreFreePosts,
                        onClickIgnoreFiles = onClickIgnoreFiles,
                    )
                }
            }

            items(
                items = posts,
                key = { it.post.id.uniqueValue },
            ) {
                CreatorPostsDownloadItem(
                    modifier = Modifier
                        .animateContentSize()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    data = it,
                )
            }
        }
    }
}
