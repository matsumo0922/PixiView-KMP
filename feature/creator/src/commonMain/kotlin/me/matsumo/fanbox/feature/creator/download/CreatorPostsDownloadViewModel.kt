package me.matsumo.fanbox.feature.creator.download

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.repository.FanboxRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class CreatorPostsDownloadViewModel(
    private val fanboxRepository: FanboxRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {
    private val _uiState = MutableStateFlow(CreatorPostsDownloadUiState(emptyList(), false))

    val uiState = _uiState.asStateFlow()

    fun fetch(creatorId: CreatorId) {
        viewModelScope.launch {
            _uiState.value = suspendRunCatching {
                val paginate = fanboxRepository.getCreatorPostsPaginate(creatorId)
                val posts = paginate.map { async(ioDispatcher) { fanboxRepository.getCreatorPosts(creatorId, it, 10) } }

                posts.map { it.await().contents }.flatten()
            }.fold(
                onSuccess = { CreatorPostsDownloadUiState(it, true) },
                onFailure = { CreatorPostsDownloadUiState(emptyList(), false) },
            )
        }
    }
}

@Stable
data class CreatorPostsDownloadUiState(
    val posts: List<FanboxPost>,
    val isReady: Boolean,
)
