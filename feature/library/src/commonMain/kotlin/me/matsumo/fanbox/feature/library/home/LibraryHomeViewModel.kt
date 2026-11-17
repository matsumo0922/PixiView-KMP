package me.matsumo.fanbox.feature.library.home

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.PagingData
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.datastore.OfferBannerDataStore
import me.matsumo.fanbox.core.logs.category.OffersLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.OfferBanner
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.resolveOfferBanner
import me.matsumo.fanbox.core.model.resolveOfferCycle
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.extensition.emptyPaging
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class LibraryHomeViewModel(
    private val settingRepository: SettingRepository,
    private val fanboxRepository: FanboxRepository,
    private val billingClient: BillingClient,
    private val offerBannerDataStore: OfferBannerDataStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        LibraryUiState(
            setting = Setting.default(),
            bookmarkedPostsIds = emptyList(),
            homePaging = emptyPaging(),
            supportedPaging = emptyPaging(),
        ),
    )

    val uiState = _uiState.asStateFlow()
    val updatePlusTrigger = settingRepository.updatePlusMode

    private val offerPlans = MutableStateFlow(emptyList<BillingPlan>())
    private val currentEpochSeconds = MutableStateFlow(nowEpochSeconds())

    /** 表示のログを送ったバナー。オファーの種類と周期の組ごとに、表示のログを 1 回だけ送るため。 */
    private val shownOfferBanners = mutableSetOf<Pair<String, Int>>()

    /** ホームの投稿一覧の最上部に表示するオファーのバナー。表示しない場合は null。 */
    val offerBanner = combine(
        settingRepository.setting,
        offerPlans,
        offerBannerDataStore.dismissedCycleNumber,
        currentEpochSeconds,
    ) { setting, plans, dismissedCycleNumber, currentEpochSeconds ->
        resolveOfferBanner(
            hasPrivilege = setting.hasPrivilege,
            plans = plans,
            offerCycle = resolveOfferCycle(setting.firstLaunchTime, currentEpochSeconds),
            dismissedCycleNumber = dismissedCycleNumber,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
        initialValue = null,
    )

    init {
        viewModelScope.launch {
            settingRepository.setting.collectLatest { userData ->
                val loadSize = if (userData.isHideRestricted || userData.isUseGridMode) 20 else 10
                val isHideRestricted = userData.isHideRestricted

                _uiState.value = uiState.value.copy(
                    setting = userData,
                    homePaging = fanboxRepository.getHomePostsPager(loadSize, isHideRestricted),
                    supportedPaging = fanboxRepository.getSupportedPostsPager(loadSize, isHideRestricted),
                )
            }
        }

        viewModelScope.launch {
            fanboxRepository.bookmarkedPostsIds.collectLatest {
                _uiState.value = uiState.value.copy(bookmarkedPostsIds = it)
            }
        }

        // オファーのバナーは Android だけで表示する
        if (currentPlatform == Platform.Android) {
            viewModelScope.launch { loadOfferPlans() }
        }
    }

    /**
     * オファーの判定に使うプランを取得する。
     *
     * オファーがある間は、提示の残り時間と周期の切り替わりをバナーに反映できるよう、現在の時刻を 1 秒ごとに更新する。
     */
    private suspend fun loadOfferPlans() {
        offerPlans.value = suspendRunCatching { billingClient.getPlans() }.getOrNull()?.plans.orEmpty()

        val hasOffer = offerPlans.value.any { plan -> plan.trialPeriod != null || plan.introOffer != null }
        if (!hasOffer) return

        while (true) {
            currentEpochSeconds.value = nowEpochSeconds()
            delay(1.seconds)
        }
    }

    fun onOfferBannerShown(banner: OfferBanner) {
        val isFirstShown = shownOfferBanners.add(banner.logValue to banner.cycleNumber)
        if (!isFirstShown) return

        OffersLog.bannerShown(banner.logValue, banner.cycleNumber).send()
    }

    fun onOfferBannerTapped(banner: OfferBanner) {
        OffersLog.bannerTapped(banner.logValue, banner.cycleNumber).send()
    }

    fun onOfferBannerDismissed(banner: OfferBanner) {
        OffersLog.bannerDismissed(banner.logValue, banner.cycleNumber).send()

        viewModelScope.launch {
            offerBannerDataStore.setDismissedCycleNumber(banner.cycleNumber)
        }
    }

    fun postLike(postId: PostId) {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.likePost(postId)
            }
        }
    }

    fun postBookmark(post: Post, isBookmarked: Boolean) {
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
    val setting: Setting,
    val bookmarkedPostsIds: List<PostId>,
    val homePaging: Flow<PagingData<Post>>,
    val supportedPaging: Flow<PagingData<Post>>,
)

@OptIn(ExperimentalTime::class)
private fun nowEpochSeconds(): Long = Clock.System.now().epochSeconds
