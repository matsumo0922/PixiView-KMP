package me.matsumo.fanbox.feature.post.queue

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import me.matsumo.fanbox.core.model.FanboxDownloadItems
import me.matsumo.fanbox.core.repository.DownloadPostsRepository

class DownloadQueueViewModel(
    private val downloadPostsRepository: DownloadPostsRepository,
): ViewModel() {

    val reservingPosts = downloadPostsRepository.reservingPosts
    val downloadState = downloadPostsRepository.downloadState

    fun cancelDownload(key: String) {
        downloadPostsRepository.cancelDownload(key)
    }
}
