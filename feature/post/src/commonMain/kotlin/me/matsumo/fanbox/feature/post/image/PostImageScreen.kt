package me.matsumo.fanbox.feature.post.image

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.animation.Zoomable
import me.matsumo.fanbox.core.ui.animation.rememberZoomableState
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.IndicatorPlaceHolder
import me.matsumo.fanbox.core.ui.extensition.LocalSnackbarHostState
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.ToastExtension
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.core.ui.theme.center
import me.matsumo.fanbox.feature.post.image.items.PostImageMenuDialog
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun PostImageRoute(
    postId: PostId,
    postImageIndex: Int,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostImageViewModel = koinViewModel(),
    snackExtension: ToastExtension = koinInject(),
) {
    val scope = rememberCoroutineScope()
    val snackbarHostState = LocalSnackbarHostState.current
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()

    LaunchedEffect(postId) {
        if (screenState !is ScreenState.Idle) {
            viewModel.fetch(postId)
        }
    }

    AsyncLoadContents(
        modifier = modifier,
        screenState = screenState,
        retryAction = { viewModel.fetch(postId) },
    ) { uiState ->
        PostImageScreen(
            modifier = Modifier.fillMaxSize(),
            imageIndex = postImageIndex,
            postDetail = uiState.postDetail,
            onClickDownload = {
                viewModel.downloadImages(it) {
                    scope.launch {
                        snackExtension.show(snackbarHostState, MR.strings.common_downloaded)
                    }
                }
            },
            onTerminate = terminate,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun PostImageScreen(
    imageIndex: Int,
    postDetail: FanboxPostDetail,
    onClickDownload: (List<FanboxPostDetail.ImageItem>) -> Unit,
    onTerminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(initialPage = imageIndex) { postDetail.body.imageItems.size }
    var isShowMenu by remember { mutableStateOf(false) }

    Box(modifier) {
        HorizontalPager(
            modifier = Modifier.fillMaxSize(),
            state = pagerState,
            // beyondBoundsPageCount = 1,
        ) {
            val zoomState = rememberZoomableState()

            Box(Modifier.fillMaxSize()) {
                Zoomable(
                    modifier = Modifier.fillMaxSize(),
                    state = zoomState,
                    onLongPress = { isShowMenu = true },
                ) {
                    SubcomposeAsyncImage(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .fillMaxWidth(),
                        model = ImageRequest.Builder(LocalPlatformContext.current)
                            .fanboxHeader()
                            .data(postDetail.body.imageItems[it].thumbnailUrl)
                            .build(),
                        loading = {
                            IndicatorPlaceHolder()
                        },
                        contentScale = ContentScale.Fit,
                        contentDescription = null,
                    )
                }

                if (postDetail.body.imageItems[it].extension.lowercase() == "gif" && currentPlatform == Platform.IOS) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f)),
                    ) {
                        Text(
                            modifier = Modifier
                                .padding(24.dp)
                                .align(Alignment.Center),
                            text = stringResource(MR.strings.error_ios_gif_support),
                            style = MaterialTheme.typography.bodyLarge.center(),
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }

        PixiViewTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            onClickActions = { isShowMenu = true },
            onClickNavigation = onTerminate,
            isTransparent = true,
        )

        PostImageMenuDialog(
            isVisible = isShowMenu,
            onClickDownload = { onClickDownload.invoke(listOf(postDetail.body.imageItems[pagerState.settledPage])) },
            onClickAllDownload = { onClickDownload.invoke(postDetail.body.imageItems) },
            onDismissRequest = { isShowMenu = false },
        )
    }
}
