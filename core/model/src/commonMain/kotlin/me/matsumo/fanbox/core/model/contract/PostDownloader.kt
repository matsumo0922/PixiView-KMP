package me.matsumo.fanbox.core.model.contract

import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostDetail

interface PostDownloader {
    fun onDownloadImages(imageItems: List<PostDetail.ImageItem>)
    fun onDownloadFile(fileItem: PostDetail.FileItem)
    fun onDownloadPosts(posts: List<Post>, isIgnoreFree: Boolean, isIgnoreFile: Boolean)
}
