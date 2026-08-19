package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.PostItemId

data class FanboxDownloadItems(
    val postId: PostId,
    val title: String,
    val items: List<Item>,
    val requestType: RequestType,
    val key: String,
) {
    data class Item(
        val postId: PostId,
        val itemId: PostItemId,
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
        data class WholePost(
            val post: Post?,
            val isIgnoreFiles: Boolean,
        ) : RequestType
    }
}
