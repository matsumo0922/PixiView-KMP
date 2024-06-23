package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asSkiaBitmap
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import io.ktor.client.statement.readBytes
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.allocArrayOf
import kotlinx.cinterop.memScoped
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.repository.FanboxRepository
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
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

@OptIn(ExperimentalCoilApi::class)
@Composable
actual fun DrawableResource.asCoilImage(): Image {
    return imageResource(this).asSkiaBitmap().asCoilImage()
}

class ImageDownloaderImpl(
    private val fanboxRepository: FanboxRepository,
    private val scope: CoroutineScope,
): ImageDownloader {

    private var globalCallback: (() -> Unit)? = null

    override fun downloadImages(items: List<FanboxPostDetail.ImageItem>, callback: () -> Unit) {
        var count = 0

        for (item in items) {
            downloadImage(item) {
                count++

                if (count == items.size) {
                    callback()
                }
            }
        }
    }

    override fun downloadFiles(items: List<FanboxPostDetail.FileItem>, callback: () -> Unit) {
        var count = 0

        for (item in items) {
            downloadFile(item) {
                count++

                if (count == items.size) {
                    callback()
                }
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun downloadImage(item: FanboxPostDetail.ImageItem, callback: () -> Unit) {
        scope.launch {
            runCatching {
                val url = if (item.extension.lowercase() == "gif") item.originalUrl else item.thumbnailUrl
                val bytes = fanboxRepository.download(url).readBytes()
                val nsData = memScoped { NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong()) }
                val uiImage = UIImage.imageWithData(nsData)!!

                UIImageWriteToSavedPhotosAlbum(uiImage, null, null, null)

                callback.invoke()
                globalCallback?.invoke()
            }.also {
                PostsLog.download(
                    type = "image",
                    postId = item.postId.value,
                    itemId = item.id,
                    extension = item.extension,
                    isSuccess = it.isSuccess,
                ).send()
            }
        }
    }

    @OptIn(ExperimentalForeignApi::class)
    override fun downloadFile(item: FanboxPostDetail.FileItem, callback: () -> Unit) {
        scope.launch {
            runCatching {
                val path = NSHomeDirectory() + "/Documents/FANBOX"
                val name = "illust-${item.postId}-${item.id}.${item.extension}"
                val fileManager = NSFileManager.defaultManager

                if (!fileManager.fileExistsAtPath(path)) {
                    fileManager.createDirectoryAtPath(path, true, null, null)
                }

                val bytes = fanboxRepository.download(item.url).readBytes()
                val nsData = memScoped { NSData.create(bytes = allocArrayOf(bytes), length = bytes.size.toULong()) }

                NSFileHandle.fileHandleForWritingAtPath(path + name)!!.apply {
                    writeData(nsData)
                    closeFile()
                }

                callback.invoke()
            }.also {
                PostsLog.download(
                    type = "file",
                    postId = item.postId.value,
                    itemId = item.id,
                    extension = item.extension,
                    isSuccess = it.isSuccess,
                ).send()
            }
        }
    }

    override fun setCallback(callback: () -> Unit) {
        this.globalCallback = callback
    }
}