package me.matsumo.fanbox.core.repository

import android.content.Context
import android.media.MediaScannerConnection
import android.os.Environment
import android.webkit.MimeTypeMap
import androidx.core.net.toUri
import com.hippo.unifile.UniFile
import io.github.aakira.napier.Napier
import io.ktor.client.call.body
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import io.ktor.utils.io.streams.writePacket
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.datastore.PixiViewDataStore
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.fanbox.FanboxDownloadItems
import me.matsumo.fanbox.core.model.fanbox.FanboxPostDetail
import me.matsumo.fanbox.core.model.fanbox.id.PostId
import java.io.File

class DownloadPostsRepositoryImpl(
    private val context: Context,
    private val userDataStore: PixiViewDataStore,
    private val fanboxRepository: FanboxRepository,
    private val scope: CoroutineScope,
) : DownloadPostsRepository {

    private val _reservingPosts = MutableStateFlow(emptyList<FanboxDownloadItems>())
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.None)

    override val reservingPosts: StateFlow<List<FanboxDownloadItems>> = _reservingPosts.asStateFlow()
    override val downloadState: StateFlow<DownloadState> = _downloadState.asStateFlow()

    init {
        scope.launch {
            while (isActive) {
                delay(500)

                val downloadItems = _reservingPosts.getAndUpdate { it.drop(1) }.firstOrNull()

                if (downloadItems == null) {
                    _downloadState.value = DownloadState.None
                    continue
                }

                val itemProgresses = MutableList(downloadItems.items.size) { MutableStateFlow(0f) }
                val items = downloadItems.items.mapIndexed { index, item ->
                    async {
                        downloadItem(item) { progress ->
                            itemProgresses[index].value = progress

                            _downloadState.value = DownloadState.Downloading(
                                title = downloadItems.title,
                                progress = itemProgresses.sumOf { it.value.toDouble() }.toFloat() / downloadItems.items.size,
                                remainingItems = downloadItems.items.size,
                            )
                        }
                    }
                }

                val results = items.awaitAll()
                val pathAndMime = mutableListOf<Pair<String, String>>()

                for ((item, channel) in results.filterNotNull()) {
                    runCatching {
                        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.extension)
                        val parent = getParentFile(downloadItems.requestType) ?: getOldParentFile(downloadItems.requestType, mime.orEmpty())
                        val file = parent.createFile("${item.name}.${item.extension}")
                        val outputStream = context.contentResolver.openOutputStream(file!!.uri)!!

                        while (!channel.isClosedForRead) {
                            outputStream.writePacket(channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong()))
                        }

                        pathAndMime.add(file.uri.path.orEmpty() to mime.orEmpty())
                        delay(100)
                    }.onFailure {
                        Napier.e(it) { "Failed to download item: ${item.name}" }
                    }
                }

                MediaScannerConnection.scanFile(
                    context,
                    pathAndMime.map { it.first }.toTypedArray(),
                    pathAndMime.map { it.second }.toTypedArray(),
                ) { _, uri ->
                    Napier.d { "MediaScannerConnection: $uri" }
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
                title = postDetail.title,
                items = images + files,
                requestType = FanboxDownloadItems.RequestType.Post(postDetail.user.name),
                callback = callback,
            )

            _reservingPosts.update { it + items }
        }
    }

    override fun requestDownloadImages(title: String, images: List<FanboxPostDetail.ImageItem>, callback: () -> Unit) {
        val items = FanboxDownloadItems(
            title = title,
            items = images.map { it.toDownloadItem() },
            requestType = FanboxDownloadItems.RequestType.Image,
            callback = callback,
        )

        _reservingPosts.update { it + items }
    }

    override fun requestDownloadFiles(title: String, files: List<FanboxPostDetail.FileItem>, callback: () -> Unit) {
        val items = FanboxDownloadItems(
            title = title,
            items = files.map { it.toDownloadItem() },
            requestType = FanboxDownloadItems.RequestType.File,
            callback = callback,
        )

        _reservingPosts.update { it + items }
    }

    override suspend fun getSaveDirectory(requestType: FanboxDownloadItems.RequestType): String {
        val sampleMimeType = when (requestType) {
            is FanboxDownloadItems.RequestType.Image -> "image/jpeg"
            is FanboxDownloadItems.RequestType.File -> "application/octet-stream"
            is FanboxDownloadItems.RequestType.Post -> "application/octet-stream"
        }

        return (getParentFile(requestType) ?: getOldParentFile(requestType, sampleMimeType)).filePath ?: "Unknown"
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

    private suspend fun downloadItem(item: FanboxDownloadItems.Item, onDownload: (Float) -> Unit): Pair<FanboxDownloadItems.Item, ByteReadChannel>? {
        return suspendRunCatching {
            val url = if (item.extension.lowercase() != "gif") item.originalUrl else item.thumbnailUrl
            val channel = fanboxRepository.download(url, onDownload).body<ByteReadChannel>()

            onDownload.invoke(1f)

            item to channel
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

    private fun getOldParentFile(requestType: FanboxDownloadItems.RequestType, mimeType: String): UniFile {
        val parent = when {
            mimeType.contains("image") -> Environment.DIRECTORY_PICTURES
            mimeType.contains("video") -> Environment.DIRECTORY_PICTURES
            else -> Environment.DIRECTORY_DOWNLOADS
        }

        val child = when (requestType) {
            is FanboxDownloadItems.RequestType.Image -> "FANBOX"
            is FanboxDownloadItems.RequestType.File -> "FANBOX"
            is FanboxDownloadItems.RequestType.Post -> requestType.creatorName
        }

        val dir = File(Environment.getExternalStorageDirectory().path + "/$parent", "FANBOX")
        val childDir = File(dir, child)

        if (!dir.exists()) {
            dir.mkdir()
        }

        if (child.isNotBlank() && !childDir.exists()) {
            childDir.mkdir()
        }

        return UniFile.fromFile(childDir)!!
    }

    private suspend fun getParentFile(requestType: FanboxDownloadItems.RequestType): UniFile? {
        val userData = userDataStore.userData.first()

        return when (requestType) {
            is FanboxDownloadItems.RequestType.Image -> {
                if (userData.imageSaveDirectory.isBlank()) return null
                UniFile.fromUri(context, userData.imageSaveDirectory.toUri())
            }

            is FanboxDownloadItems.RequestType.File -> {
                if (userData.fileSaveDirectory.isBlank()) return null
                UniFile.fromUri(context, userData.fileSaveDirectory.toUri())
            }

            is FanboxDownloadItems.RequestType.Post -> {
                if (userData.postSaveDirectory.isBlank()) return null
                val parentFile = UniFile.fromUri(context, userData.postSaveDirectory.toUri())
                parentFile.createDirectory(requestType.creatorName)
            }
        }
    }
}
