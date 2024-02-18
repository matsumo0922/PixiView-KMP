package me.matsumo.fanbox

import androidx.compose.runtime.Stable
import com.benasher44.uuid.uuid4
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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.billing.BillingStatus
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.MR
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.time.Duration.Companion.minutes

class PixiViewViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
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
                isAppLocked = if (userData.isAppLock) isAppLocked else false,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    init {
        viewModelScope.launch {
            fanboxRepository.logoutTrigger.collectLatest {
                _isLoggedInFlow.emit(false)
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

    fun initPixiViewId() {
        viewModelScope.launch {
            userDataRepository.setPixiViewId(uuid4().toString())
        }
    }

    fun updateState() {
        viewModelScope.launch {
            suspendRunCatching {
                fanboxRepository.updateCsrfToken()
                fanboxRepository.getNewsLetters()

                fanboxRepository.metaData.firstOrNull()?.also {
                    userDataRepository.setTestUser(it.context.user.userId == "100912340")
                }
            }.isSuccess.also {
                Napier.d { "update home state. isLoggedIn: $it" }
                _isLoggedInFlow.emit(it)
            }
        }
    }

    fun setAppLock(isAppLock: Boolean) {
        viewModelScope.launch {
            _isAppLockedFlow.emit(if (userDataRepository.userData.first().isAppLock) isAppLock else false)
        }
    }

    suspend fun tryToAuthenticate(biometryAuthenticator: BiometryAuthenticator): Boolean = suspendRunCatching {
        biometryAuthenticator.checkBiometryAuthentication(
            requestTitle = MR.strings.home_app_lock_title.desc(),
            requestReason = MR.strings.home_app_lock_message.desc(),
            failureButtonText = MR.strings.error_no_data.desc(),
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
