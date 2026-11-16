package me.matsumo.fanbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.aakira.napier.Napier
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.logs.category.ReviewsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.LaunchLogRepository
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.feature.service.NewContentCheckScheduler

class MainViewModel(
    settingRepository: SettingRepository,
    downloadPostsRepository: DownloadPostsRepository,
    private val launchLogRepository: LaunchLogRepository,
    private val newContentCheckScheduler: NewContentCheckScheduler,
) : ViewModel() {

    val setting = settingRepository.setting

    private val _isAdsSdkInitialized = MutableStateFlow(false)
    val isAdsSdkInitialized = _isAdsSdkInitialized.asStateFlow()

    private val _reviewRequest = Channel<Unit>(Channel.CONFLATED)

    /** アプリ内レビューを依頼するタイミング。Activity が前面にいるときに受け取る。 */
    val reviewRequest = _reviewRequest.receiveAsFlow()

    init {
        viewModelScope.launch {
            // 予約が増減するたびではなく、空から空でない状態に変わったときだけ判定する
            downloadPostsRepository.reservingPosts
                .map { reservingPosts -> reservingPosts.isNotEmpty() }
                .distinctUntilChanged()
                .filter { hasReservingPosts -> hasReservingPosts }
                .collect { onDownloadReservationStarted() }
        }

        // 設定画面での切り替えも、保存された値の変化としてここで受け取る。
        viewModelScope.launch {
            settingRepository.storedSetting
                .map { setting -> NewContentChecks.of(setting) }
                .distinctUntilChanged()
                .collect { checks -> updateNewContentCheck(checks) }
        }
    }

    fun setAdsSdkInitialized() {
        _isAdsSdkInitialized.value = true
    }

    private suspend fun updateNewContentCheck(checks: NewContentChecks) {
        suspendRunCatching {
            newContentCheckScheduler.update(checks.isPostCheckEnabled, checks.isMessageCheckEnabled)
        }.onFailure { failure ->
            Napier.w(failure) { "Failed to update the new content check. checks=$checks" }
        }
    }

    private suspend fun onDownloadReservationStarted() {
        // 使い始めたばかりのユーザーには依頼しない
        val isEligible = launchLogRepository.getLaunchCount() >= 3

        ReviewsLog.tryRequestReview(isEligible).send()

        if (isEligible) {
            _reviewRequest.send(Unit)
        }
    }
}

/** 定期実行で、新着投稿・新着メッセージの確認をそれぞれ行うか。 */
private data class NewContentChecks(
    val isPostCheckEnabled: Boolean,
    val isMessageCheckEnabled: Boolean,
) {
    companion object {
        /** [setting] の通知の設定から決める。テストユーザーは、どちらの確認も行わない。 */
        fun of(setting: Setting) = NewContentChecks(
            isPostCheckEnabled = setting.isNewPostNotificationEnabled && !setting.isTestUser,
            isMessageCheckEnabled = setting.isNewMessageNotificationEnabled && !setting.isTestUser,
        )
    }
}
