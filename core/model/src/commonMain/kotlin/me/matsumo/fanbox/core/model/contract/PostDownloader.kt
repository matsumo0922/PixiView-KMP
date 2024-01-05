package me.matsumo.fanbox.core.model.contract

import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail

interface PostDownloader {
    fun onDownloadImages(imageItems: List<FanboxPostDetail.ImageItem>)
    fun onDownloadFile(fileItem: FanboxPostDetail.FileItem)
    fun onDownloadPosts(posts: List<FanboxPost>, isIgnoreFree: Boolean, isIgnoreFile: Boolean)
}
