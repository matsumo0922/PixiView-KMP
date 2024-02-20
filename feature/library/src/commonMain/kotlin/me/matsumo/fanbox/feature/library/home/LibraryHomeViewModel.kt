package me.matsumo.fanbox.feature.library.home

import androidx.compose.runtime.Stable
import androidx.paging.PagingData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope

class LibraryHomeViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    // private val verifiedPlusUseCase: VerifyPlusUseCase,
    // private val nativeAdsPreLoader: NativeAdsPreLoader,
    private val pixiViewConfig: PixiViewConfig,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LibraryUiState(
            userData = UserData.default(),
            bookmarkedPosts = emptyList(),
            homePaging = emptyPaging(),
            supportedPaging = emptyPaging(),
            nativeAdUnitId = pixiViewConfig.adMobAndroid.nativeAdUnitId,
        ),
    )

    val uiState = _uiState.asStateFlow()
    val updatePlusTrigger = userDataRepository.updatePlusMode

    // val adsPreLoader = nativeAdsPreLoader

    init {
        viewModelScope.launch {
            userDataRepository.userData.collectLatest { userData ->
                val loadSize = if (userData.isHideRestricted || userData.isGridMode) 20 else 10
                val isHideRestricted = userData.isHideRestricted

                _uiState.value = uiState.value.copy(
                    userData = userData,
                    homePaging = fanboxRepository.getHomePostsPager(loadSize, isHideRestricted),
                    supportedPaging = fanboxRepository.getSupportedPostsPager(loadSize, isHideRestricted),
                    nativeAdUnitId = pixiViewConfig.adMobAndroid.nativeAdUnitId,
                )
            }
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPosts.collectLatest {
                _uiState.value = uiState.value.copy(bookmarkedPosts = it)
            }
        }

        /*viewModelScope.launch {
            adsPreLoader.preloadAd()
        }*/
    }

    fun postLike(postId: PostId) {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.likePost(postId)
            }
        }
    }

    fun postBookmark(post: FanboxPost, isBookmarked: Boolean) {
        viewModelScope.launch {
            suspendRunCatching {
                if (isBookmarked) {
                    fanboxRepository.bookmarkPost(post)
                } else {
                    fanboxRepository.unbookmarkPost(post)
                }
            }
        }
    }
}

@Stable
data class LibraryUiState(
    val userData: UserData,
    val bookmarkedPosts: List<PostId>,
    val homePaging: Flow<PagingData<FanboxPost>>,
    val supportedPaging: Flow<PagingData<FanboxPost>>,
    val nativeAdUnitId: String,
)
