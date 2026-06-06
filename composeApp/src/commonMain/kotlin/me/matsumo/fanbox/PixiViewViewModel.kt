package me.matsumo.fanbox

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.biometry.BiometryAuthenticator
import dev.icerock.moko.resources.desc.desc
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.datastore.LaunchLogDataStore
import me.matsumo.fanbox.core.datastore.OldCookieDataStore
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_no_data
import me.matsumo.fanbox.core.resources.home_app_lock_message
import me.matsumo.fanbox.core.resources.home_app_lock_title
import me.matsumo.fanbox.core.ui.extensition.getFanboxMetadataDummy
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import org.jetbrains.compose.resources.getString
import kotlin.time.Clock
import kotlin.time.Duration.Companion.minutes
import kotlin.time.ExperimentalTime
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PixiViewViewModel(
    private val settingRepository: SettingRepository,
    private val fanboxRepository: FanboxRepository,
    private val downloadPostsRepository: DownloadPostsRepository,
    private val launchLogDataStore: LaunchLogDataStore,
    private val oldCookieDataStore: OldCookieDataStore,
    private val billingClient: BillingClient,
    private val pixiViewConfig: PixiViewConfig,
) : ViewModel() {

    private val _isLoggedInFlow: MutableSharedFlow<Boolean> = MutableSharedFlow(replay = 1)
    private val _isAppLockedFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val _metadataFlow: MutableStateFlow<FanboxMetaData> = MutableStateFlow(getFanboxMetadataDummy())

    // インタースティシャル広告の頻度判定用。永続化せずプロセス内のみ保持する
    private var interstitialPostCloseCount = 0
    private var lastInterstitialShownEpochSeconds = 0L

    val screenState = combine(
        listOf(
            settingRepository.setting,
            fanboxRepository.sessionId,
            downloadPostsRepository.downloadState,
            _metadataFlow,
            _isLoggedInFlow,
            _isAppLockedFlow,
        ),
    ) { flows ->
        val setting = flows[0] as Setting
        val sessionId = flows[1] as String?
        val downloadState = flows[2] as DownloadState
        val fanboxMetadata = flows[3] as FanboxMetaData
        val isLoggedIn = flows[4] as Boolean
        val isAppLocked = flows[5] as Boolean

        ScreenState.Idle(
            MainUiState(
                setting = setting,
                sessionId = sessionId.orEmpty(),
                fanboxMetadata = fanboxMetadata,
                downloadState = downloadState,
                isLoggedIn = isLoggedIn,
                isAppLocked = if (setting.isUseAppLock) isAppLocked else false,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ScreenState.Loading,
    )

    init {
        launchLogDataStore.launch()

        viewModelScope.launch {
            fanboxRepository.logoutTrigger.collectLatest {
                _isLoggedInFlow.emit(false)
            }
        }

        viewModelScope.launch {
            settingRepository.setting.collectLatest {
                LogConfigurator.configure(
                    pixiViewConfig = pixiViewConfig,
                    setting = it,
                )
            }
        }

        viewModelScope.launch {
            while (isActive) {
                updateState()
                delay(10.minutes)
            }
        }
    }

    fun billingClientUpdate() {
        viewModelScope.launch {
            delay(3000)

            suspendRunCatching { billingClient.hasPlus() }.onSuccess {
                settingRepository.setPlusMode(it)
            }
        }
    }

    @OptIn(ExperimentalUuidApi::class, ExperimentalTime::class)
    fun initPixiViewId() {
        viewModelScope.launch {
            settingRepository.setPixiViewId(Uuid.random().toString())
        }
    }

    @OptIn(ExperimentalTime::class)
    fun initFirstLaunchTime() {
        viewModelScope.launch {
            settingRepository.setFirstLaunchTime(Clock.System.now().epochSeconds)
        }
    }

    fun updateState() {
        viewModelScope.launch {
            suspendRunCatching {
                if (!settingRepository.setting.first().isTestUser) {
                    val oldCookies = oldCookieDataStore.getCookies()
                    val sessionId = oldCookies.map { it.split("=") }.firstOrNull { it.first() == "FANBOXSESSID" }?.get(1)

                    if (sessionId != null) {
                        fanboxRepository.setSessionId(sessionId)
                        oldCookieDataStore.save("")
                    }

                    _metadataFlow.value = suspendRunCatching { fanboxRepository.getMetadata() }.getOrElse { getFanboxMetadataDummy() }

                    fanboxRepository.updateCsrfToken()
                    fanboxRepository.getNewsLetters()
                }
            }.isSuccess.also {
                Napier.d { "update home state. isLoggedIn: $it" }
                _isLoggedInFlow.emit(it)
            }
        }
    }

    fun setAppLock(isAppLock: Boolean) {
        viewModelScope.launch {
            _isAppLockedFlow.emit(if (settingRepository.setting.first().isUseAppLock) isAppLock else false)
        }
    }

    @OptIn(ExperimentalTime::class)
    suspend fun onPostDetailClosedForInterstitialAd(
        showInterstitialAd: suspend () -> Boolean,
    ) {
        val setting = settingRepository.setting.first()
        if (!setting.canHandlePostDetailInterstitialAd()) return

        val currentEpochSeconds = Clock.System.now().epochSeconds
        interstitialPostCloseCount += 1

        val canShowInterstitialAd = shouldShowPostDetailInterstitialAd(
            postCloseCount = interstitialPostCloseCount,
            currentEpochSeconds = currentEpochSeconds,
            lastShownEpochSeconds = lastInterstitialShownEpochSeconds,
        )
        if (!canShowInterstitialAd) return

        if (showInterstitialAd()) {
            interstitialPostCloseCount = 0
            lastInterstitialShownEpochSeconds = currentEpochSeconds
        }
    }

    suspend fun tryToAuthenticate(biometryAuthenticator: BiometryAuthenticator): Boolean = suspendRunCatching {
        biometryAuthenticator.checkBiometryAuthentication(
            requestTitle = getString(Res.string.home_app_lock_title).desc(),
            requestReason = getString(Res.string.home_app_lock_message).desc(),
            failureButtonText = getString(Res.string.error_no_data).desc(),
            allowDeviceCredentials = true,
        )
    }.fold(
        onSuccess = { it },
        onFailure = { false },
    )
}

private fun Setting.canHandlePostDetailInterstitialAd(): Boolean {
    return !hasPrivilege && shouldShowInterstitialAd
}

private fun shouldShowPostDetailInterstitialAd(
    postCloseCount: Int,
    currentEpochSeconds: Long,
    lastShownEpochSeconds: Long,
): Boolean {
    val reachesTriggerCount = postCloseCount >= INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT
    val matchesTriggerInterval = postCloseCount % INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT == 0
    val elapsedSeconds = currentEpochSeconds - lastShownEpochSeconds
    val satisfiesCooldown = elapsedSeconds >= INTERSTITIAL_AD_COOLDOWN_SECONDS

    return reachesTriggerCount && matchesTriggerInterval && satisfiesCooldown
}

/** インタースティシャル広告を表示する投稿詳細クローズ回数。 */
private const val INTERSTITIAL_POST_CLOSE_TRIGGER_COUNT = 3

/** インタースティシャル広告表示後に再表示を抑制する秒数。 */
private const val INTERSTITIAL_AD_COOLDOWN_SECONDS = 180L

/** アプリ全体の表示状態をまとめた UI モデル。 */
@Stable
data class MainUiState(
    val setting: Setting,
    val sessionId: String,
    val fanboxMetadata: FanboxMetaData,
    val downloadState: DownloadState,
    val isLoggedIn: Boolean,
    val isAppLocked: Boolean,
)
