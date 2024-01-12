package me.matsumo.fanbox.feature.post.image

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import coil3.compose.LocalPlatformContext
import coil3.compose.SubcomposeAsyncImage
import coil3.request.ImageRequest
import com.mxalbert.zoomable.Zoomable
import com.mxalbert.zoomable.rememberZoomableState
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.ui.AsyncLoadContents
import me.matsumo.fanbox.core.ui.component.PixiViewTopBar
import me.matsumo.fanbox.core.ui.extensition.IndicatorPlaceHolder
import me.matsumo.fanbox.core.ui.extensition.fanboxHeader
import me.matsumo.fanbox.feature.post.image.items.PostImageMenuDialog
import moe.tlaster.precompose.flow.collectAsStateWithLifecycle
import moe.tlaster.precompose.koin.koinViewModel

@Composable
internal fun PostImageRoute(
    postId: PostId,
    postImageIndex: Int,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostImageViewModel = koinViewModel(PostImageViewModel::class),
) {
    // val postDownloader = context as PostDownloader
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
            onClickDownload = { /*postDownloader::onDownloadImages*/ },
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
            beyondBoundsPageCount = 1,
        ) {
            val zoomState = rememberZoomableState()

            Zoomable(
                modifier = Modifier.fillMaxSize(),
                state = zoomState,
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
        }

        PixiViewTopBar(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .fillMaxWidth(),
            onClickActions = { isShowMenu = true },
            onClickNavigation = onTerminate,
            isTransparent = true,
        )

        if (isShowMenu) {
            PostImageMenuDialog(
                onClickDownload = { onClickDownload.invoke(listOf(postDetail.body.imageItems[pagerState.settledPage])) },
                onClickAllDownload = { onClickDownload.invoke(postDetail.body.imageItems) },
                onDismissRequest = { isShowMenu = false },
            )
        }
    }
}
