package me.matsumo.fanbox.core.repository

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.webkit.MimeTypeMap
import io.ktor.client.call.body
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.streams.writePacket
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
import java.io.File

class DownloadPostsRepositoryImpl(
    private val context: Context,
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

                for ((item, channel) in results.filterNotNull()) {
                    runCatching {
                        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.extension)
                        val uri = getUri(context, "${item.name}.${item.extension}", getParentDirName(downloadItems.requestType), mime.orEmpty())
                        val outputStream = context.contentResolver.openOutputStream(uri!!)!!

                        while (!channel.isClosedForRead) {
                            outputStream.writePacket(channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong()))
                        }

                        delay(100)
                    }
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

    private suspend fun downloadItem(item: FanboxDownloadItems.Item): Pair<FanboxDownloadItems.Item, ByteReadChannel>? {
        return suspendRunCatching {
            val url = if (item.extension.lowercase() == "gif") item.originalUrl else item.thumbnailUrl
            item to fanboxRepository.download(url).body<ByteReadChannel>()
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

    private fun getUri(context: Context, name: String, child: String, mimeType: String = ""): Uri? {
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

    private fun getParentDirName(requestType: FanboxDownloadItems.RequestType?): String = when (requestType) {
        is FanboxDownloadItems.RequestType.Image -> "FANBOX"
        is FanboxDownloadItems.RequestType.File -> "FANBOX"
        is FanboxDownloadItems.RequestType.Post -> requestType.creatorName
        else -> "FANBOX"
    }
}
