package me.matsumo.fanbox.core.model.contract

import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail

interface PostDownloader {
    fun onDownloadImages(imageItems: List<FanboxPostDetail.ImageItem>)
    fun onDownloadFile(fileItem: FanboxPostDetail.FileItem)
    fun onDownloadPosts(posts: List<FanboxPost>, isIgnoreFree: Boolean, isIgnoreFile: Boolean)
}
