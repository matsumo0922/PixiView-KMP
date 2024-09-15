package me.matsumo.fanbox.core.model.fanbox.entity


import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxCreatorPostItemsEntity(
    @SerialName("body")
    val body: List<Body>
) {
    @Serializable
    data class Body(
        @SerialName("commentCount")
        val commentCount: Int,
        @SerialName("cover")
        val cover: Cover?,
        @SerialName("creatorId")
        val creatorId: String,
        @SerialName("excerpt")
        val excerpt: String,
        @SerialName("feeRequired")
        val feeRequired: Int,
        @SerialName("hasAdultContent")
        val hasAdultContent: Boolean,
        @SerialName("id")
        val id: String,
        @SerialName("isLiked")
        val isLiked: Boolean,
        @SerialName("isRestricted")
        val isRestricted: Boolean,
        @SerialName("likeCount")
        val likeCount: Int,
        @SerialName("publishedDatetime")
        val publishedDatetime: String,
        @SerialName("tags")
        val tags: List<String>,
        @SerialName("title")
        val title: String,
        @SerialName("updatedDatetime")
        val updatedDatetime: String,
        @SerialName("user")
        val user: User
    ) {
        @Serializable
        data class Cover(
            @SerialName("type")
            val type: String,
            @SerialName("url")
            val url: String
        )

        @Serializable
        data class User(
            @SerialName("iconUrl")
            val iconUrl: String?,
            @SerialName("name")
            val name: String,
            @SerialName("userId")
            val userId: String
        )
    }
}
