package me.matsumo.fanbox.core.ui.extensition

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import coil3.Image
import coil3.annotation.ExperimentalCoilApi
import coil3.asCoilImage
import dev.icerock.moko.resources.ImageResource
import io.ktor.client.statement.bodyAsChannel
import io.ktor.utils.io.jvm.javaio.copyTo
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.repository.FanboxRepository
import java.io.File

@OptIn(ExperimentalCoilApi::class)
@Composable
actual fun ImageResource.asCoilImage(): Image {
    return getDrawable(LocalContext.current)?.asCoilImage() ?: error("can't read Drawable of $this")
}

class ImageDownloaderImpl(
    private val context: Context,
    private val fanboxRepository: FanboxRepository,
): ImageDownloader {

    override suspend fun downloadImage(item: FanboxPostDetail.ImageItem, updateCallback: (Float) -> Unit): Boolean = suspendRunCatching {
        val url = if (item.extension.lowercase() == "gif") item.originalUrl else item.thumbnailUrl
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.extension)
        val uri = getUri(context, "illust-${item.postId}-${item.id}.${item.extension}", Environment.DIRECTORY_PICTURES, "FANBOX", mime.orEmpty())
        val outputStream = context.contentResolver.openOutputStream(uri!!)!!

        fanboxRepository.download(url, updateCallback)
            .bodyAsChannel()
            .copyTo(outputStream)
    }.isSuccess

    override suspend fun downloadFile(item: FanboxPostDetail.FileItem, updateCallback: (Float) -> Unit): Boolean = suspendRunCatching {
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.extension)
        val uri = getUri(context, "illust-${item.postId}-${item.id}.${item.extension}", Environment.DIRECTORY_DOWNLOADS, "FANBOX", mime.orEmpty())
        val outputStream = context.contentResolver.openOutputStream(uri!!)!!

        fanboxRepository.download(item.url, updateCallback)
            .bodyAsChannel()
            .copyTo(outputStream)
    }.isSuccess

    private fun getUri(context: Context, name: String, parent: String, child: String = "", mimeType: String = ""): Uri? {
        val contentResolver = context.contentResolver
        val contentValues = ContentValues().apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val path = when {
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

        return contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
    }
}
