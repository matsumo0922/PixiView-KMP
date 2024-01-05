package me.matsumo.fanbox.core.model.fanbox

import me.matsumo.fanbox.core.model.fanbox.id.PostId
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.common.serializer.LocalDateTimeSerializer

@Serializable
data class FanboxPost(
    val id: PostId,
    val title: String,
    val cover: FanboxCover?,
    val user: FanboxUser,
    val excerpt: String,
    val feeRequired: Int,
    val hasAdultContent: Boolean,
    val isLiked: Boolean,
    var isBookmarked: Boolean,
    val isRestricted: Boolean,
    val likeCount: Int,
    val commentCount: Int,
    val tags: List<String>,
    @Serializable(with = LocalDateTimeSerializer::class)
    val publishedDatetime: LocalDateTime,
    @Serializable(with = LocalDateTimeSerializer::class)
    val updatedDatetime: LocalDateTime,
) {
    companion object {
        fun dummy() = FanboxPost(
            id = PostId(""),
            title = "週末こっそり配信絵 Vol.159",
            excerpt = "~23:30 くらいまで、軽く配信します～！！",
            publishedDatetime = LocalDateTime.parse("2024-01-01T00:00:00"),
            cover = FanboxCover(
                url = "https://downloads.fanbox.cc/images/post/6894357/w/1200/kcksgQDZpzodzjrvTrlJ834X.jpeg",
                type = "image/jpeg",
            ),
            isLiked = false,
            isBookmarked = false,
            likeCount = 12,
            commentCount = 3,
            feeRequired = 500,
            isRestricted = false,
            hasAdultContent = false,
            tags = emptyList(),
            updatedDatetime = LocalDateTime.parse("2024-01-01T00:00:00"),
            user = FanboxUser.dummy(),
        )
    }
}
