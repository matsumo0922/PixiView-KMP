package me.matsumo.fanbox.core.repository

import io.ktor.client.call.body
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.fanbox.FanboxDownloadItems
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import platform.Foundation.NSData
import platform.Foundation.NSFileHandle
import platform.Foundation.NSFileManager
import platform.Foundation.NSHomeDirectory
import platform.Foundation.closeFile
import platform.Foundation.create
import platform.Foundation.fileHandleForWritingAtPath
import platform.Foundation.writeData
import platform.UIKit.UIImage
import platform.UIKit.UIImageWriteToSavedPhotosAlbum

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
class DownloadPostsRepositoryImpl(
    private val fanboxRepository: FanboxRepository,
    private val scope: CoroutineScope,
) : DownloadPostsRepository {

    private var _reservingPosts = MutableStateFlow(emptyList<FanboxDownloadItems>())

    override val reservingPosts: StateFlow<List<FanboxDownloadItems>> = _reservingPosts.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                delay(500)

                val downloadItems = _reservingPosts.getAndUpdate { it.drop(1) }.firstOrNull() ?: continue
                val items = downloadItems.items.map {
                    async { downloadItem(it) }
                }

                val results = items.awaitAll()

                for ((item, bytes) in results.filterNotNull()) {
                    saveItem(item, downloadItems.requestType, bytes)
                }

                downloadItems.callback.invoke()
            }
        }
    }

    override fun requestDownloadPost(postId: PostId, callback: () -> Unit) {
        scope.launch {
            val postDetail = fanboxRepository.getPost(postId)
            val images = postDetail.body.imageItems.map { it.toDownloadItem() }
            val files = postDetail.body.fileItems.map { it.toDownloadItem() }
            val items = FanboxDownloadItems(
                items = images + files,
                requestType = FanboxDownloadItems.RequestType.Post(postDetail.user.name),
                callback = callback,
            )

            _reservingPosts.update { it + items }
        }
    }

    override fun requestDownloadImages(images: List<FanboxPostDetail.ImageItem>, callback: () -> Unit) {
        val items = FanboxDownloadItems(
            items = images.map { it.toDownloadItem() },
            requestType = FanboxDownloadItems.RequestType.Image,
            callback = callback,
        )

        _reservingPosts.update { it + items }
    }

    override fun requestDownloadFiles(files: List<FanboxPostDetail.FileItem>, callback: () -> Unit) {
        val items = FanboxDownloadItems(
            items = files.map { it.toDownloadItem() },
            requestType = FanboxDownloadItems.RequestType.File,
            callback = callback,
        )

        _reservingPosts.update { it + items }
    }

    private fun FanboxPostDetail.ImageItem.toDownloadItem(): FanboxDownloadItems.Item {
        return FanboxDownloadItems.Item(
            postId = postId,
            name = "image-$postId-$id",
            extension = extension,
            originalUrl = originalUrl,
            thumbnailUrl = thumbnailUrl,
            type = FanboxDownloadItems.Item.Type.Image,
        )
    }

    private fun FanboxPostDetail.FileItem.toDownloadItem(): FanboxDownloadItems.Item {
        return FanboxDownloadItems.Item(
            postId = postId,
            name = "file-$postId-$id",
            extension = extension,
            originalUrl = url,
            thumbnailUrl = "",
            type = FanboxDownloadItems.Item.Type.File,
        )
    }

    private suspend fun downloadItem(item: FanboxDownloadItems.Item): Pair<FanboxDownloadItems.Item, ByteArray>? {
        return suspendRunCatching {
            val url = if (item.extension.lowercase() == "gif") item.originalUrl else item.thumbnailUrl
            item to fanboxRepository.download(url).body<ByteArray>()
        }.also {
            PostsLog.download(
                type = "unknown",
                postId = item.postId.value,
                itemId = item.name,
                extension = item.extension,
                isSuccess = it.isSuccess,
            ).send()
        }.getOrNull()
    }

    private fun saveItem(item: FanboxDownloadItems.Item, requestType: FanboxDownloadItems.RequestType, bytes: ByteArray) {
        runCatching {
            when (item.type) {
                FanboxDownloadItems.Item.Type.Image -> {
                    val nsData = memScoped { NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong()) }
                    val uiImage = UIImage.imageWithData(nsData)!!

                    UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)
                }
                FanboxDownloadItems.Item.Type.File -> {
                    val path = NSHomeDirectory() + "/Documents/FANBOX/${getParentDirName(requestType)}"
                    val nsData = memScoped { NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong()) }
                    val fileManager = NSFileManager.defaultManager

                    if (!fileManager.fileExistsAtPath(path)) {
                        fileManager.createDirectoryAtPath(path, true, null, null)
                    }

                    NSFileHandle.fileHandleForWritingAtPath(path + item.name)!!.apply {
                        writeData(nsData)
                        closeFile()
                    }
                }
            }
        }
    }

    private fun getParentDirName(requestType: FanboxDownloadItems.RequestType?): String = when (requestType) {
        is FanboxDownloadItems.RequestType.Image -> "images"
        is FanboxDownloadItems.RequestType.File -> "files"
        is FanboxDownloadItems.RequestType.Post -> requestType.creatorName
        else -> "FANBOX"
    }
}
