package me.matsumo.fanbox.feature.setting.top

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dev.icerock.moko.biometry.BiometryAuthenticator
import dev.icerock.moko.resources.desc.desc
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.DownloadFileType
import me.matsumo.fanbox.core.model.Flag
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.UserData
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fanbox.core.repository.FlagRepository
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.error_no_data
import me.matsumo.fanbox.core.resources.home_app_lock_message
import me.matsumo.fanbox.core.resources.home_app_lock_title
import me.matsumo.fanbox.core.ui.extensition.getFanboxMetadataDummy
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import org.jetbrains.compose.resources.getString

class SettingTopViewModel(
    private val userDataRepository: UserDataRepository,
    private val fanboxRepository: FanboxRepository,
    private val flagRepository: FlagRepository,
    private val pixiViewConfig: PixiViewConfig,
) : ViewModel() {

    val screenState = combine(userDataRepository.userData, fanboxRepository.sessionId, ::Pair).map { (userData, sessionId) ->
        ScreenState.Idle(
            SettingTopUiState(
                userData = userData,
                metaData = suspendRunCatching { fanboxRepository.getMetadata() }.getOrElse { getFanboxMetadataDummy() },
                fanboxSessionId = sessionId ?: "Unknown",
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

    fun setDownloadFileType(downloadFileType: DownloadFileType) {
        viewModelScope.launch {
            userDataRepository.setDownloadFileType(downloadFileType)
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

    fun setAutoImagePreview(isAutoImagePreview: Boolean) {
        viewModelScope.launch {
            userDataRepository.setAutoImagePreview(isAutoImagePreview)
        }
    }

    fun setReshowReveal() {
        viewModelScope.launch {
            flagRepository.setFlag(Flag.REVEAL_CREATOR_TOP, true)
            flagRepository.setFlag(Flag.REVEAL_POST_DETAIL, true)
            flagRepository.setFlag(Flag.SHOULD_REQUEST_DOMAIN_VERIFICATION, true)
        }
    }

    fun setDeveloperMode(isDeveloperMode: Boolean) {
        viewModelScope.launch {
            userDataRepository.setDeveloperMode(isDeveloperMode)
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
data class SettingTopUiState(
    val userData: UserData,
    val metaData: FanboxMetaData,
    val fanboxSessionId: String,
    val config: PixiViewConfig,
)
