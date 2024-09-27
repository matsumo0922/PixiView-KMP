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
import me.matsumo.fanbox.core.logs.logger.LogConfigurator
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.error_no_data
import me.matsumo.fanbox.core.ui.home_app_lock_message
import me.matsumo.fanbox.core.ui.home_app_lock_title
import org.jetbrains.compose.resources.getString
import kotlin.time.Duration.Companion.minutes
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

class PixiViewViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val launchLogDataStore: LaunchLogDataStore,
    private val pixiViewConfig: PixiViewConfig,
    private val billingStatus: BillingStatus,
) : ViewModel() {

    private val _isLoggedInFlow: MutableSharedFlow<Boolean> = MutableSharedFlow(replay = 1)
    private val _isAppLockedFlow: MutableStateFlow<Boolean> = MutableStateFlow(true)

    val screenState = combine(
        userDataRepository.userData,
        fanboxRepository.cookie,
        fanboxRepository.metaData,
        _isLoggedInFlow,
        _isAppLockedFlow,
    ) { userData, cookie, metadata, isLoggedIn, isAppLocked ->
        ScreenState.Idle(
            MainUiState(
                userData = userData,
                fanboxCookie = cookie,
                fanboxMetadata = metadata,
                isLoggedIn = isLoggedIn,
                isAppLocked = if (userData.isUseAppLock) isAppLocked else false,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
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
            allowDeviceCredentials = true
        )
    }.fold(
        onSuccess = { it },
        onFailure = { false },
    )
}

@Stable
data class MainUiState(
    val userData: UserData,
    val fanboxCookie: String,
    val fanboxMetadata: FanboxMetaData,
    val isLoggedIn: Boolean,
    val isAppLocked: Boolean,
)
