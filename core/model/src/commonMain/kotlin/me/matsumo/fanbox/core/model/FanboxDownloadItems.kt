package me.matsumo.fanbox.core.model

import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostItemId

data class FanboxDownloadItems(
    val postId: FanboxPostId,
    val title: String,
    val items: List<Item>,
    val requestType: RequestType,
    val key: String,
) {
    data class Item(
        val postId: FanboxPostId,
        val itemId: FanboxPostItemId,
        val name: String,
        val extension: String,
        val originalUrl: String,
        val thumbnailUrl: String,
        val type: Type,
        val progress: Float = 0f,
    ) {
        enum class Type {
            Image,
            File,
        }
    }

    sealed interface RequestType {
        data object Image : RequestType
        data object File : RequestType
        data class Post(
            val post: FanboxPost?,
            val isIgnoreFiles: Boolean,
        ) : RequestType
    }
}
