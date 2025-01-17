package me.matsumo.fanbox.feature.post.queue

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.library_navigation_queue
import me.matsumo.fanbox.core.resources.queue_empty_description
import me.matsumo.fanbox.core.resources.queue_empty_title
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.view.ErrorView
import me.matsumo.fanbox.feature.post.queue.components.DownloadQueueItem
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun DownloadQueueScreen(
    navigateToPostDetail: (FanboxPostId) -> Unit,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: DownloadQueueViewModel = koinViewModel(),
) {
    val state = rememberLazyListState()
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    val reservingPosts by viewModel.reservingPosts.collectAsStateWithLifecycle()
    val downloadState by viewModel.downloadState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            PixiViewTopBar(
                modifier = Modifier.fillMaxWidth(),
                title = stringResource(Res.string.library_navigation_queue),
                onClickNavigation = terminate,
                scrollBehavior = scrollBehavior,
            )
        }
    ) { padding ->
        AnimatedContent(
            modifier = Modifier.fillMaxSize(),
            targetState = downloadState,
            contentKey = { it::class.simpleName },
            transitionSpec = { fadeIn().togetherWith(fadeOut()) }
        ) { currentState ->
            when (currentState) {
                is DownloadState.Downloading -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = state,
                        verticalArrangement = Arrangement.spacedBy(16.dp),
                        contentPadding = padding,
                    ) {
                        items(
                            items = listOf(currentState.items, *reservingPosts.toTypedArray()),
                            key = { it.key },
                        ) { items ->
                            DownloadQueueItem(
                                modifier = Modifier
                                    .animateItem()
                                    .fillMaxWidth()
                                    .clickable { navigateToPostDetail.invoke(items.postId) }
                                    .padding(horizontal = 16.dp),
                                items = items,
                                progress = currentState.progress.takeIf { items.key == currentState.items.key } ?: -1f,
                                onCancelClicked = { viewModel.cancelDownload(it) },
                            )
                        }
                    }
                }

                is DownloadState.None -> {
                    ErrorView(
                        modifier = Modifier
                            .padding(padding)
                            .fillMaxSize(),
                        title = Res.string.queue_empty_title,
                        message = Res.string.queue_empty_description,
                        serviceStatus = false,
                    )
                }
            }
        }
    }
}
