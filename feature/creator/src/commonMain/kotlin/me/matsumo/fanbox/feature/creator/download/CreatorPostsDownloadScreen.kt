package me.matsumo.fanbox.feature.creator.download

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Download
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxCreatorDetail
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.common_completed
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.creator_posts_download_button
import me.matsumo.fanbox.core.ui.creator_posts_download_button_downloading
import me.matsumo.fanbox.core.ui.creator_posts_download_dialog_title
import me.matsumo.fanbox.core.ui.creator_posts_download_title
import me.matsumo.fanbox.core.ui.view.SimpleAlertContents
import me.matsumo.fanbox.feature.creator.download.items.CreatorPostsDownloadItem
import me.matsumo.fanbox.feature.creator.download.items.CreatorPostsDownloadSettingsSection
import me.matsumo.fanbox.feature.creator.download.items.CreatorPostsDownloadUserSection
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun CreatorPostsDownloadRoute(
    creatorId: CreatorId,
    navigateToCancelDownloadAlert: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: CreatorPostsDownloadViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    var targetIndex by remember { mutableIntStateOf(-1) }
    var isCompleted by remember { mutableStateOf(false) }

    LaunchedEffect(true) {
        if (screenState !is ScreenState.Idle) {
            viewModel.fetch(creatorId)
        }
    }

    AsyncLoadContents(
        modifier = modifier,
        otherModifier = Modifier.fillMaxSize(),
        screenState = screenState,
        retryAction = { viewModel.fetch(creatorId) },
    ) { uiState ->
        CreatorPostsDownloadScreen(
            modifier = Modifier.fillMaxSize(),
            creatorDetail = uiState.creatorDetail,
            posts = uiState.targetPosts,
            targetIndex = targetIndex,
            isCompleted = isCompleted,
            isIgnoreFreePosts = uiState.isIgnoreFreePosts,
            isIgnoreFiles = uiState.isIgnoreFiles,
            isPrepared = uiState.isPrepared,
            ignoreKeyword = uiState.ignoreKeyword,
            onUpdateIgnoreKeyword = viewModel::updateIgnoreKeyword,
            onClickIgnoreFreePosts = viewModel::updateIgnoreFreePosts,
            onClickIgnoreFiles = viewModel::updateIgnoreFiles,
            onClickDownload = {
                scope.launch {
                    for ((index, post) in uiState.targetPosts.toList().withIndex()) {
                        targetIndex = index
                        viewModel.download(postId = post.post.id)

                        delay(1000)
                    }

                    targetIndex += 1
                    isCompleted = true
                }
            },
            terminate = {
                if (isCompleted || targetIndex == -1) terminate.invoke()
                else {
                    scope.launch {
                        navigateToCancelDownloadAlert(SimpleAlertContents.CancelDownload) {
                            terminate.invoke()
                        }
                    }
                }
            },
        )

        if (!uiState.isPrepared) {
            var progress by remember { mutableStateOf(0f) }
            val progressAnimation by animateFloatAsState(
                targetValue = progress,
                animationSpec = tween(300),
            )

            LaunchedEffect(true) {
                viewModel.fetchPosts(
                    creatorId = creatorId,
                    paginate = uiState.postsPaginate,
                    updateCallback = { progress = it },
                )
            }

            LoadingDialog(progressAnimation)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun CreatorPostsDownloadScreen(
    creatorDetail: FanboxCreatorDetail,
    targetIndex: Int,
    posts: List<CreatorPostsDownloadData>,
    isCompleted: Boolean,
    isIgnoreFreePosts: Boolean,
    isIgnoreFiles: Boolean,
    isPrepared: Boolean,
    ignoreKeyword: String,
    onUpdateIgnoreKeyword: (String) -> Unit,
    onClickIgnoreFreePosts: (Boolean) -> Unit,
    onClickIgnoreFiles: (Boolean) -> Unit,
    onClickDownload: () -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var isDisplaySettings by remember { mutableStateOf(false) }

    LaunchedEffect(targetIndex) {
        if (targetIndex > 0) {
            state.animateScrollToItem(targetIndex + 1)
        }
    }

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
                onClick = { if (targetIndex == -1) onClickDownload.invoke() },
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
                    text =  when {
                        isCompleted -> stringResource(Res.string.common_completed)
                        targetIndex == -1 -> stringResource(Res.string.creator_posts_download_button, posts.size)
                        else -> stringResource(Res.string.creator_posts_download_button_downloading, targetIndex, posts.size)
                    },
                )
            }
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
                        onClickSettings = { isDisplaySettings = !isDisplaySettings },
                    )

                    AnimatedVisibility(isDisplaySettings) {
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
            }

            itemsIndexed(
                items = posts,
                key = {  _, post -> post.post.id.uniqueValue },
            ) { index, data ->
                CreatorPostsDownloadItem(
                    modifier = Modifier
                        .animateContentSize()
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    data = data,
                    isTarget = (index == targetIndex),
                    isDownloaded = (index < targetIndex),
                )
            }
        }
    }
}

@Composable
private fun LoadingDialog(progress: Float) {
    Dialog(
        onDismissRequest = { /* do nothing */ },
        properties = DialogProperties(
            dismissOnBackPress = false,
            dismissOnClickOutside = false,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight()
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.creator_posts_download_dialog_title),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "%.2f %%".format(progress * 100),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth(),
                progress = { progress },
            )
        }
    }
}
