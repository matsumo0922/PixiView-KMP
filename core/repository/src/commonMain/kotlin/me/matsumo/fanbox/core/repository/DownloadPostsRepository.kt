package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.flow.StateFlow
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.FanboxDownloadItems
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

interface DownloadPostsRepository {
    val reservingPosts: StateFlow<List<FanboxDownloadItems>>
    val downloadState: StateFlow<DownloadState>

    fun requestDownloadPost(postId: FanboxPostId, callback: () -> Unit)
    fun requestDownloadImages(title: String, images: List<FanboxPostDetail.ImageItem>, callback: () -> Unit)
    fun requestDownloadFiles(title: String, files: List<FanboxPostDetail.FileItem>, callback: () -> Unit)

    suspend fun getSaveDirectory(requestType: FanboxDownloadItems.RequestType): String
}
