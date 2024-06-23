package me.matsumo.fanbox.core.ui.extensition

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.asAndroidBitmap
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.streams.writePacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.repository.FanboxRepository
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.imageResource
import java.io.File

@OptIn(ExperimentalCoilApi::class)
@Composable
actual fun DrawableResource.asCoilImage(): Image {
    return imageResource(this).asAndroidBitmap().asCoilImage()
}

class ImageDownloaderImpl(
    private val context: Context,
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

    override fun downloadImage(item: FanboxPostDetail.ImageItem, callback: () -> Unit) {
        scope.launch {
            runCatching {
                val url = if (item.extension.lowercase() == "gif") item.originalUrl else item.thumbnailUrl
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.extension)
                val uri = getUri(context, "illust-${item.postId}-${item.id}.${item.extension}", "FANBOX", mime.orEmpty())
                val outputStream = context.contentResolver.openOutputStream(uri!!)!!

                fanboxRepository.download(url).body<ByteReadChannel>().also {
                    while (!it.isClosedForRead) {
                        outputStream.writePacket(it.readRemaining(DEFAULT_BUFFER_SIZE.toLong()))
                    }
                }

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

    override fun downloadFile(item: FanboxPostDetail.FileItem, callback: () -> Unit) {
        scope.launch {
            runCatching {
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.extension)
                val uri = getUri(context, "illust-${item.postId}-${item.id}.${item.extension}", "FANBOX", mime.orEmpty())
                val outputStream = context.contentResolver.openOutputStream(uri!!)!!

                fanboxRepository.download(item.url).body<ByteReadChannel>().also {
                    while (!it.isClosedForRead) {
                        outputStream.writePacket(it.readRemaining(DEFAULT_BUFFER_SIZE.toLong()))
                    }
                }

                callback.invoke()
                globalCallback?.invoke()
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

    private fun getUri(context: Context, name: String, child: String, mimeType: String = ""): Uri? {
        Napier.d { "getUri: $name, $mimeType"}

        val contentUri: Uri
        val parent: String

        when {
            mimeType.contains("image") -> {
                contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                parent = Environment.DIRECTORY_PICTURES
            }
            mimeType.contains("video") -> {
                contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                parent = Environment.DIRECTORY_PICTURES
            }
            else -> {
                contentUri = MediaStore.Files.getContentUri("external")
                parent = Environment.DIRECTORY_DOWNLOADS
            }
        }

        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val path = when {
                    mimeType.contains("photoshop") -> MediaStore.Files.FileColumns.RELATIVE_PATH
                    mimeType.contains("image") -> MediaStore.Images.ImageColumns.RELATIVE_PATH
                    mimeType.contains("video") -> MediaStore.Video.VideoColumns.RELATIVE_PATH
                    mimeType.contains("audio") -> MediaStore.Audio.AudioColumns.RELATIVE_PATH
                    else -> MediaStore.Files.FileColumns.RELATIVE_PATH
                }

                put(path, "$parent/FANBOX" + if (child.isBlank()) "" else "/$child")
            } else {
                val path = Environment.getExternalStorageDirectory().path + "/$parent/FANBOX" + if (child.isEmpty()) "" else "/$child"
                val dir = File(Environment.getExternalStorageDirectory().path + "/$parent", "FANBOX")
                val childDir = File(dir, child)

                if (!dir.exists()) {
                    dir.mkdir()
                }

                if (child.isNotBlank() && !childDir.exists()) {
                    childDir.mkdir()
                }

                put(MediaStore.Images.ImageColumns.DATA, path + name)
            }
            put(MediaStore.Images.ImageColumns.DISPLAY_NAME, name)
            put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis())
        }

        return contentResolver.insert(contentUri, contentValues)
    }

    override fun setCallback(callback: () -> Unit) {
        this.globalCallback = callback
    }
}