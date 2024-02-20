package me.matsumo.fanbox.feature.post.detail

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Creator
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Home
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Search
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Supported
import me.matsumo.fanbox.feature.post.detail.PostDetailPagingType.Unknown
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class PostDetailRootViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val pixiViewConfig: PixiViewConfig,
    // private val nativeAdsPreLoader: NativeAdsPreLoader,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        PostDetailRootUiState(
            paging = null,
            nativeAdUnitId = pixiViewConfig.adMobAndroid.nativeAdUnitId,
        ),
    )

    val uiState = _uiState.asStateFlow()

    // val adsPreLoader = nativeAdsPreLoader

    /*init {
        viewModelScope.launch {
            adsPreLoader.preloadAd()
        }
    }*/

    fun fetch(type: PostDetailPagingType) {
        viewModelScope.launch {
            val userData = userDataRepository.userData.first()
            val loadSize = if (userData.isHideRestricted || userData.isGridMode) 20 else 10
            val isHideRestricted = userData.isHideRestricted

            _uiState.value = PostDetailRootUiState(
                paging = when (type) {
                    Home -> fanboxRepository.getHomePostsPagerCache(loadSize, isHideRestricted)
                    Supported -> fanboxRepository.getSupportedPostsPagerCache(loadSize, isHideRestricted)
                    Creator -> fanboxRepository.getCreatorPostsPagerCache()
                    Search -> fanboxRepository.getPostsFromQueryPagerCache()
                    Unknown -> emptyPaging()
                },
                nativeAdUnitId = pixiViewConfig.adMobAndroid.nativeAdUnitId,
            )
        }
    }
}

@Stable
data class PostDetailRootUiState(
    val paging: Flow<PagingData<FanboxPost>>?,
    val nativeAdUnitId: String,
)
