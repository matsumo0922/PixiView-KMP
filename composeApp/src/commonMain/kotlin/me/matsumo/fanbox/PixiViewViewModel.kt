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
import me.matsumo.fanbox.core.billing.BillingStatus
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.datastore.LaunchLogDataStore
import me.matsumo.fanbox.core.datastore.OldCookieDataStore
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_no_data
import me.matsumo.fanbox.core.resources.home_app_lock_message
import me.matsumo.fanbox.core.resources.home_app_lock_title
import me.matsumo.fanbox.core.ui.extensition.getFanboxMetadataDummy
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PixiViewViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val downloadPostsRepository: DownloadPostsRepository,
    private val launchLogDataStore: LaunchLogDataStore,
    private val oldCookieDataStore: OldCookieDataStore,
    private val pixiViewConfig: PixiViewConfig,
    private val billingStatus: BillingStatus,
) : ViewModel() {

    private val _isLoggedInFlow: MutableSharedFlow<Boolean> = MutableSharedFlow(replay = 1)
    private val _isAppLockedFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)
    private val _metadataFlow: MutableStateFlow<FanboxMetaData> = MutableStateFlow(getFanboxMetadataDummy())

    val screenState = combine(
        listOf(
            userDataRepository.userData,
            fanboxRepository.sessionId,
            downloadPostsRepository.downloadState,
            _metadataFlow,
            _isLoggedInFlow,
            _isAppLockedFlow,
        ),
    ) { flows ->
        val userData = flows[0] as UserData
        val sessionId = flows[1] as String?
        val downloadState = flows[2] as DownloadState
        val fanboxMetadata = flows[3] as FanboxMetaData
        val isLoggedIn = flows[4] as Boolean
        val isAppLocked = flows[5] as Boolean

        ScreenState.Idle(
            MainUiState(
                userData = userData,
                sessionId = sessionId.orEmpty(),
                fanboxMetadata = fanboxMetadata,
                downloadState = downloadState,
                isLoggedIn = isLoggedIn,
                isAppLocked = if (userData.isUseAppLock) isAppLocked else false,
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
            userDataRepository.userData.collectLatest {
                LogConfigurator.configure(
                    pixiViewConfig = pixiViewConfig,
                    userData = it,
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

    fun billingClientInitialize() {
        billingStatus.init()
    }

    fun billingClientFinish() {
        billingStatus.finish()
    }

    fun billingClientUpdate() {
        billingStatus.update()
    }

    @OptIn(ExperimentalUuidApi::class)
    fun initPixiViewId() {
        viewModelScope.launch {
            userDataRepository.setPixiViewId(Uuid.random().toString())
        }
    }

    fun updateState() {
        viewModelScope.launch {
            suspendRunCatching {
                if (!userDataRepository.userData.first().isTestUser) {
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
            _isAppLockedFlow.emit(if (userDataRepository.userData.first().isUseAppLock) isAppLock else false)
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

@Stable
data class MainUiState(
    val userData: UserData,
    val sessionId: String,
    val fanboxMetadata: FanboxMetaData,
    val downloadState: DownloadState,
    val isLoggedIn: Boolean,
    val isAppLocked: Boolean,
)
