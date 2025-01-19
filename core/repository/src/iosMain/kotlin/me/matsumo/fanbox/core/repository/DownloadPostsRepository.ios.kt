package me.matsumo.fanbox.core.repository

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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.getAndUpdate
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.datastore.PixiViewDataStore
import me.matsumo.fanbox.core.logs.category.PostsLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.DownloadFileType
import me.matsumo.fanbox.core.model.DownloadState
import me.matsumo.fanbox.core.model.FanboxDownloadItems
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@OptIn(ExperimentalForeignApi::class, ExperimentalUuidApi::class)
class DownloadPostsRepositoryImpl(
    private val fanboxRepository: FanboxRepository,
    private val userDataStore: PixiViewDataStore,
    private val scope: CoroutineScope,
) : DownloadPostsRepository {

    private val semaphore = Semaphore(2)

    private var _reservingPosts = MutableStateFlow(emptyList<FanboxDownloadItems>())
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

                val items = when (val type = downloadItems.requestType) {
                    FanboxDownloadItems.RequestType.File -> downloadItems.items
                    FanboxDownloadItems.RequestType.Image -> downloadItems.items
                    is FanboxDownloadItems.RequestType.Post -> {
                        val postDetail = fanboxRepository.getPostDetail(downloadItems.postId)
                        val images = postDetail.body.imageItems.map { it.toDownloadItem() }
                        val files = postDetail.body.fileItems.map { it.toDownloadItem() }

                        images + if (type.isIgnoreFiles) emptyList() else files
                    }
                }

                val itemProgresses = MutableList(items.size) { MutableStateFlow(0f) }
                val data = items.mapIndexed { index, item ->
                    async {
                        semaphore.withPermit {
                            downloadItem(item) { progress ->
                                itemProgresses[index].value = progress

                                _downloadState.value = DownloadState.Downloading(
                                    items = downloadItems,
                                    progress = itemProgresses.sumOf { it.value.toDouble() }.toFloat() / items.size,
                                )
                            }
                        }
                    }
                }

                val results = data.awaitAll()

                for ((item, bytes) in results.filterNotNull()) {
                    saveItem(item, downloadItems.requestType, bytes)
                }
            }
        }
    }

    override fun cancelDownload(key: String) {
        _reservingPosts.update { posts ->
            posts.filter { it.key != key }
        }
    }

    override fun requestDownloadPost(post: FanboxPost, isIgnoreFiles: Boolean) {
        scope.launch {
            val items = FanboxDownloadItems(
                postId = post.id,
                title = post.title,
                items = emptyList(),
                requestType = FanboxDownloadItems.RequestType.Post(post, isIgnoreFiles),
                key = Uuid.random().toHexString(),
            )

            _reservingPosts.update { it + items }
        }
    }

    override fun requestDownloadImages(postId: FanboxPostId, title: String, images: List<FanboxPostDetail.ImageItem>) {
        val items = FanboxDownloadItems(
            postId = postId,
            title = title,
            items = images.map { it.toDownloadItem() },
            requestType = FanboxDownloadItems.RequestType.Image,
            key = Uuid.random().toHexString(),
        )

        _reservingPosts.update { it + items }
    }

    override fun requestDownloadFiles(postId: FanboxPostId, title: String, files: List<FanboxPostDetail.FileItem>) {
        val items = FanboxDownloadItems(
            postId = postId,
            title = title,
            items = files.map { it.toDownloadItem() },
            requestType = FanboxDownloadItems.RequestType.File,
            key = Uuid.random().toHexString(),
        )

        _reservingPosts.update { it + items }
    }

    override suspend fun getSaveDirectory(requestType: FanboxDownloadItems.RequestType): String {
        return "Unknown"
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

    private suspend fun downloadItem(item: FanboxDownloadItems.Item, onDownload: (Float) -> Unit): Pair<FanboxDownloadItems.Item, ByteArray>? {
        return suspendRunCatching {
            val fileType = userDataStore.userData.first().downloadFileType
            val url = if (item.extension.lowercase() != "gif" || fileType == DownloadFileType.ORIGINAL) item.originalUrl else item.thumbnailUrl
            val channel = fanboxRepository.download(url, onDownload).body<ByteArray>()

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
        is FanboxDownloadItems.RequestType.Post -> requestType.post.user?.name ?: "UnknownUser"
        else -> "FANBOX"
    }
}
