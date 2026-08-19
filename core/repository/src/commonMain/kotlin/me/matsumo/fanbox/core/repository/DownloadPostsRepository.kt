package me.matsumo.fanbox.core.repository

import kotlinx.coroutines.flow.StateFlow
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.FanboxDownloadItems
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostDetail
import me.matsumo.fanbox.core.model.fanbox.PostId

interface DownloadPostsRepository {
    val reservingPosts: StateFlow<List<FanboxDownloadItems>>
    val downloadState: StateFlow<DownloadState>

    fun cancelDownload(key: String)

    fun requestDownloadPost(post: Post, isIgnoreFiles: Boolean)
    fun requestDownloadImages(postId: PostId, title: String, images: List<PostDetail.ImageItem>)
    fun requestDownloadFiles(postId: PostId, title: String, files: List<PostDetail.FileItem>)

    suspend fun getSaveDirectory(requestType: FanboxDownloadItems.RequestType): String
}
