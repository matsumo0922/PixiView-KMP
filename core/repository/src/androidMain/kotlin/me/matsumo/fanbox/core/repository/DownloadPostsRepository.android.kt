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
import kotlinx.io.readByteArray
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.datastore.PixiViewDataStore
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.DownloadFileType
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.FanboxDownloadItems
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import java.io.File

class DownloadPostsRepositoryImpl(
    private val context: Context,
    private val userDataStore: PixiViewDataStore,
    private val fanboxRepository: FanboxRepository,
    private val scope: CoroutineScope,
) : DownloadPostsRepository {

    private val _reservingPosts = MutableStateFlow(emptyList<FanboxDownloadItems>())
    private val _downloadState = MutableStateFlow<DownloadState>(DownloadState.None)
    private val _downloadedItems = MutableStateFlow(emptyList<DownloadedItems>())

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

                _downloadedItems.update { it + DownloadedItems(downloadItems, items.awaitAll()) }
                downloadItems.callback.invoke()
            }
        }

        scope.launch {
            while (isActive) {
                val downloadedItems = _downloadedItems.getAndUpdate { it.drop(1) }.firstOrNull()
                if (downloadedItems == null) {
                    delay(1000)
                    continue
                }

                saveFiles(downloadedItems.downloadItems, downloadedItems.results)
            }
        }
    }

    override fun requestDownloadPost(postId: FanboxPostId, callback: () -> Unit) {
        scope.launch {
            val postDetail = fanboxRepository.getPostDetail(postId)
            val images = postDetail.body.imageItems.map { it.toDownloadItem() }
            val files = postDetail.body.fileItems.map { it.toDownloadItem() }
            val items = FanboxDownloadItems(
                title = postDetail.title,
                items = images + files,
                requestType = FanboxDownloadItems.RequestType.Post(postDetail.user?.name.orEmpty()),
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
        return (getParentFile(requestType) ?: getOldParentFile(requestType, true)).filePath ?: "Unknown"
    }

    private fun FanboxPostDetail.ImageItem.toDownloadItem(): FanboxDownloadItems.Item {
        return FanboxDownloadItems.Item(
            postId = postId,
            itemId = id,
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
            itemId = id,
            name = "file-$postId-$id",
            extension = extension,
            originalUrl = url,
            thumbnailUrl = "",
            type = FanboxDownloadItems.Item.Type.File,
        )
    }

    private suspend fun downloadItem(item: FanboxDownloadItems.Item, onDownload: (Float) -> Unit): Pair<FanboxDownloadItems.Item, File>? {
        return suspendRunCatching {
            val fileType = userDataStore.userData.first().downloadFileType
            val url = if (item.extension.lowercase() != "gif" || fileType == DownloadFileType.ORIGINAL) item.originalUrl else item.thumbnailUrl

            val tmpFile = File(context.cacheDir, "tmp-${item.name}.${item.extension}")

            fanboxRepository.download(url, onDownload).execute { response ->
                val channel = response.body<ByteReadChannel>()

                while (!channel.isClosedForRead) {
                    val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())

                    while (!packet.exhausted()) {
                        tmpFile.appendBytes(packet.readByteArray())
                    }
                }

                onDownload.invoke(1f)
            }

            item to tmpFile
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

    private fun getOldParentFile(requestType: FanboxDownloadItems.RequestType, isDryRun: Boolean = false): UniFile {
        val environmentDir = when (requestType) {
            is FanboxDownloadItems.RequestType.Image -> Environment.DIRECTORY_PICTURES
            is FanboxDownloadItems.RequestType.File -> Environment.DIRECTORY_DOWNLOADS
            is FanboxDownloadItems.RequestType.Post -> Environment.DIRECTORY_DOWNLOADS
        }

        val child = when (requestType) {
            is FanboxDownloadItems.RequestType.Image -> "FANBOX"
            is FanboxDownloadItems.RequestType.File -> "FANBOX"
            is FanboxDownloadItems.RequestType.Post -> requestType.creatorName
        }

        val dir = File("${Environment.getExternalStorageDirectory().path}/$environmentDir", "FANBOX")
        val childDir = File(dir, child)

        if (!isDryRun) {
            if (!dir.exists()) {
                dir.mkdir()
            }

            if (child.isNotBlank() && !childDir.exists()) {
                childDir.mkdir()
            }
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

    private suspend fun saveFiles(downloadItems: FanboxDownloadItems, results: List<Pair<FanboxDownloadItems.Item, File>?>) {
        val pathAndMime = mutableListOf<Pair<String, String>>()

        for ((item, tmpFile) in results.filterNotNull()) {
            runCatching {
                val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(item.extension)
                val parent = getParentFile(downloadItems.requestType) ?: getOldParentFile(downloadItems.requestType)
                val file = parent.createFile("${item.name}.${item.extension}")
                val modifiedTime = System.currentTimeMillis()

                tmpFile.setLastModified(modifiedTime)
                file.filePath?.let { File(it).setLastModified(modifiedTime) }

                context.contentResolver.openOutputStream(file!!.uri)!!.use {
                    tmpFile.inputStream().use { input ->
                        input.copyTo(it)
                    }
                }

                tmpFile.delete()
                pathAndMime.add(file.uri.path.orEmpty() to mime.orEmpty())

                delay(1000)
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
    }

    private data class DownloadedItems(
        val downloadItems: FanboxDownloadItems,
        val results: List<Pair<FanboxDownloadItems.Item, File>?>,
    )
}
