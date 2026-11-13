package me.matsumo.fanbox

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.logs.category.ReviewsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.repository.DownloadPostsRepository
import me.matsumo.fanbox.core.repository.LaunchLogRepository
import me.matsumo.fanbox.core.repository.SettingRepository

class MainViewModel(
    settingRepository: SettingRepository,
    downloadPostsRepository: DownloadPostsRepository,
    private val launchLogRepository: LaunchLogRepository,
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
    }

    fun setAdsSdkInitialized() {
        _isAdsSdkInitialized.value = true
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
