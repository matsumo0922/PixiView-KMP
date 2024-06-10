package me.matsumo.fanbox.feature.setting.top

import androidx.compose.runtime.Stable
import dev.icerock.moko.biometry.BiometryAuthenticator
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.MR
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope

class SettingTopViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val pixiViewConfig: PixiViewConfig,
) : ViewModel() {

    val screenState = combine(userDataRepository.userData, fanboxRepository.cookie, fanboxRepository.metaData, ::Triple).map { (userData, cookie, metaData) ->
        val cookieMap = cookie.split(";")
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .associate { it.split("=", limit = 2).let { item -> item[0] to item[1] } }

        ScreenState.Idle(
            SettingTopUiState(
                userData = userData,
                metaData = metaData,
                fanboxSessionId = cookieMap["FANBOXSESSID"] ?: "unknown",
                config = pixiViewConfig,
            ),
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    suspend fun logout(): Result<Unit> {
        return suspendRunCatching {
            fanboxRepository.logout()
        }
    }

    fun setAppLock(isAppLock: Boolean) {
        viewModelScope.launch {
            userDataRepository.setUseAppLock(isAppLock)
        }
    }

    fun setFollowTabDefaultHome(isFollowTabDefaultHome: Boolean) {
        viewModelScope.launch {
            userDataRepository.setFollowTabDefaultHome(isFollowTabDefaultHome)
        }
    }

    fun setHideAdultContents(isHideAdultContents: Boolean) {
        viewModelScope.launch {
            userDataRepository.setHideAdultContents(isHideAdultContents)
        }
    }

    fun setOverrideAdultContents(isOverrideAdultContents: Boolean) {
        viewModelScope.launch {
            userDataRepository.setOverrideAdultContents(isOverrideAdultContents)
        }
    }

    fun setUseInfinityPostDetail(isInfinityPostDetail: Boolean) {
        viewModelScope.launch {
            userDataRepository.setUseInfinityPostDetail(isInfinityPostDetail)
        }
    }

    fun setHideRestricted(isHideRestricted: Boolean) {
        viewModelScope.launch {
            userDataRepository.setHideRestricted(isHideRestricted)
        }
    }

    fun setGridMode(isGridMode: Boolean) {
        viewModelScope.launch {
            userDataRepository.setUseGridMode(isGridMode)
        }
    }

    fun setDeveloperMode(isDeveloperMode: Boolean) {
        viewModelScope.launch {
            userDataRepository.setDeveloperMode(isDeveloperMode)
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
data class SettingTopUiState(
    val userData: UserData,
    val metaData: FanboxMetaData,
    val fanboxSessionId: String,
    val config: PixiViewConfig,
)
