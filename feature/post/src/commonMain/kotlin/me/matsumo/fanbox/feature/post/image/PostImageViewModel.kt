package me.matsumo.fanbox.feature.post.image

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.error_network

class PostImageViewModel(
    private val fanboxRepository: FanboxRepository,
    private val downloadPostsRepository: DownloadPostsRepository,
) : ViewModel() {

    private val _screenState = MutableStateFlow<ScreenState<PostImageUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    fun fetch(postId: PostId) {
        viewModelScope.launch {
            _screenState.value = ScreenState.Loading
            _screenState.value = suspendRunCatching {
                PostImageUiState(
                    postDetail = fanboxRepository.getPostCached(postId),
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = { ScreenState.Error(Res.string.error_network) },
            )
        }
    }

    fun downloadImages(imageItems: List<FanboxPostDetail.ImageItem>, callback: () -> Unit) {
        downloadPostsRepository.requestDownloadImages(imageItems, callback)
    }
}

@Stable
data class PostImageUiState(
    val postDetail: FanboxPostDetail,
)
