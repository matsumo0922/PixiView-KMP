package me.matsumo.fanbox.feature.post.detail

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Creator
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Home
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Search
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Supported
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Unknown

class PostDetailRootViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PostDetailRootUiState(
            paging = null,
            userData = UserData.default(),
        ),
    )

    val uiState = _uiState.asStateFlow()

    fun fetch(type: PostDetailPagingType) {
        viewModelScope.launch {
            val userData = userDataRepository.userData.first()
            val loadSize = if (userData.isHideRestricted || userData.isUseGridMode) 20 else 10
            val isHideRestricted = userData.isHideRestricted

            _uiState.value = PostDetailRootUiState(
                paging = when (type) {
                    Home -> fanboxRepository.getHomePostsPagerCache(loadSize, isHideRestricted)
                    Supported -> fanboxRepository.getSupportedPostsPagerCache(loadSize, isHideRestricted)
                    Creator -> fanboxRepository.getCreatorPostsPagerCache()
                    Search -> fanboxRepository.getPostsFromQueryPagerCache()
                    Unknown -> emptyPaging()
                },
                userData = userData,
            )
        }
    }
}

@Stable
data class PostDetailRootUiState(
    val paging: Flow<PagingData<FanboxPost>>?,
    val userData: UserData,
)
