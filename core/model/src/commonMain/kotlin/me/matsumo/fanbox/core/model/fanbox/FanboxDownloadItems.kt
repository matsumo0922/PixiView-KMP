package me.matsumo.fanbox.core.model.fanbox

import me.matsumo.fanbox.core.model.fanbox.id.PostId

data class FanboxDownloadItems(
    val title: String,
    val items: List<Item>,
    val requestType: RequestType,
    val callback: () -> Unit,
) {
    data class Item(
        val postId: PostId,
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
        data class Post(val creatorName: String) : RequestType
    }
}
