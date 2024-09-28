package me.matsumo.fanbox.core.model.fanbox.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxCommentsEntity(
    @SerialName("items")
    val items: List<Item>,
    @SerialName("nextUrl")
    val nextUrl: String?,
) {
    @Serializable
    data class Item(
        @SerialName("body")
        val body: String,
        @SerialName("createdDatetime")
        val createdDatetime: String,
        @SerialName("id")
        val id: String,
        @SerialName("isLiked")
        val isLiked: Boolean,
        @SerialName("isOwn")
        val isOwn: Boolean,
        @SerialName("likeCount")
        val likeCount: Int,
        @SerialName("parentCommentId")
        val parentCommentId: String,
        @SerialName("rootCommentId")
        val rootCommentId: String,
        @SerialName("user")
        val user: User?,
        @SerialName("replies")
        val replies: List<Item> = emptyList(),
    ) {
        @Serializable
        data class User(
            @SerialName("iconUrl")
            val iconUrl: String?,
            @SerialName("name")
            val name: String,
            @SerialName("userId")
            val userId: String,
        )
    }
}
