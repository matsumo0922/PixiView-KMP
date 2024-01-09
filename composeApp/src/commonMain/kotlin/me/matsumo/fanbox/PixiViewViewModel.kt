package me.matsumo.fanbox

import androidx.compose.runtime.Stable
import com.benasher44.uuid.uuid4
import io.github.aakira.napier.Napier
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import moe.tlaster.precompose.viewmodel.ViewModel
import moe.tlaster.precompose.viewmodel.viewModelScope
import kotlin.time.Duration.Companion.minutes

class PixiViewViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
): ViewModel() {

    private val _isLoggedInFlow: MutableSharedFlow<Boolean> = MutableSharedFlow(replay = 1)

    val screenState = combine(
        userDataRepository.userData,
        fanboxRepository.cookie,
        fanboxRepository.metaData,
        _isLoggedInFlow,
    ) { userData, cookie, metadata, isLoggedIn ->
        ScreenState.Idle(
            MainUiState(
                userData = userData,
                fanboxCookie = cookie,
                fanboxMetadata = metadata,
                isLoggedIn = isLoggedIn,
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
}

@Stable
data class MainUiState(
    val userData: UserData,
    val fanboxCookie: String,
    val fanboxMetadata: FanboxMetaData,
    val isLoggedIn: Boolean,
)
