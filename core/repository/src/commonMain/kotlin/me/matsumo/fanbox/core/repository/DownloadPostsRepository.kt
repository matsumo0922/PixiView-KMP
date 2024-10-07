package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.flow.StateFlow
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.fanbox.FanboxDownloadItems
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.PostId

interface DownloadPostsRepository {
    val reservingPosts: StateFlow<List<FanboxDownloadItems>>
    val downloadState: StateFlow<DownloadState>

    fun requestDownloadPost(postId: PostId, callback: () -> Unit)
    fun requestDownloadImages(images: List<FanboxPostDetail.ImageItem>, callback: () -> Unit)
    fun requestDownloadFiles(files: List<FanboxPostDetail.FileItem>, callback: () -> Unit)

    suspend fun getSaveDirectory(requestType: FanboxDownloadItems.RequestType): String
}
