package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.flow.StateFlow
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.FanboxDownloadItems
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

interface DownloadPostsRepository {
    val reservingPosts: StateFlow<List<FanboxDownloadItems>>
    val downloadState: StateFlow<DownloadState>

    fun cancelDownload(key: String)

    fun requestDownloadPost(post: FanboxPost, isIgnoreFiles: Boolean)
    fun requestDownloadImages(postId: FanboxPostId, title: String, images: List<FanboxPostDetail.ImageItem>)
    fun requestDownloadFiles(postId: FanboxPostId, title: String, files: List<FanboxPostDetail.FileItem>)

    suspend fun getSaveDirectory(requestType: FanboxDownloadItems.RequestType): String
}
