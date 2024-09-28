package me.matsumo.fanbox.core.model.fanbox

import kotlinx.datetime.Instant
import me.matsumo.fanbox.core.model.fanbox.id.PostId

data class FanboxPostDetail(
    val id: PostId,
    val title: String,
    val body: Body,
    val coverImageUrl: String?,
    val commentCount: Int,
    val excerpt: String,
    val feeRequired: Int,
    val hasAdultContent: Boolean,
    val imageForShare: String,
    val isLiked: Boolean,
    var isBookmarked: Boolean,
    val isRestricted: Boolean,
    val likeCount: Int,
    val tags: List<String>,
    val updatedDatetime: Instant,
    val publishedDatetime: Instant,
    val nextPost: OtherPost?,
    val prevPost: OtherPost?,
    val user: FanboxUser,
) {
    val browserUrl get() = "https://www.fanbox.cc/@${user.creatorId}/posts/$id"

    fun asPost(): FanboxPost {
        return FanboxPost(
            id = id,
            title = title,
            excerpt = excerpt,
            cover = FanboxCover(
                url = coverImageUrl ?: body.imageItems.firstOrNull()?.thumbnailUrl.orEmpty(),
                type = "From Detail",
            ),
            hasAdultContent = hasAdultContent,
            isLiked = isLiked,
            isBookmarked = isBookmarked,
            isRestricted = isRestricted,
            likeCount = likeCount,
            commentCount = commentCount,
            updatedDatetime = updatedDatetime,
            publishedDatetime = publishedDatetime,
            feeRequired = feeRequired,
            user = user,
            tags = tags,
        )
    }

    sealed interface Body {
        val imageItems
            get() = when (this) {
                is Article -> blocks.filterIsInstance<Article.Block.Image>().map { it.item }
                is Image -> images
                is File -> files.mapNotNull { it.asImageItem() }
                is Unknown -> emptyList()
            }

        val fileItems
            get() = when (this) {
                is Article -> blocks.filterIsInstance<Article.Block.File>().map { it.item }
                is Image -> emptyList()
                is File -> files
                is Unknown -> emptyList()
            }

        data class Article(val blocks: List<Block>) : Body {
            sealed interface Block {
                data class Text(val text: String) : Block

                data class Image(val item: ImageItem) : Block

                data class File(val item: FileItem) : Block

                data class Link(
                    val html: String?,
                    val post: FanboxPost?,
                ) : Block
            }
        }

        data class Image(
            val text: String,
            val images: List<ImageItem>,
        ) : Body

        data class File(
            val text: String,
            val files: List<FileItem>,
        ) : Body

        data object Unknown : Body
    }

    data class OtherPost(
        val id: PostId,
        val title: String,
        val publishedDatetime: Instant,
    )

    data class ImageItem(
        val id: String,
        val postId: PostId,
        val extension: String,
        val originalUrl: String,
        val thumbnailUrl: String,
        val aspectRatio: Float,
    )

    data class VideoItem(
        val id: String,
        val postId: PostId,
        val extension: String,
        val url: String,
    )

    data class FileItem(
        val id: String,
        val postId: PostId,
        val name: String,
        val extension: String,
        val size: Long,
        val url: String,
    ) {
        fun asImageItem(): ImageItem? {
            return if (!extension.lowercase().contains(Regex("""(jpg|jpeg|png|gif)"""))) null
            else ImageItem(
                id = id,
                postId = postId,
                extension = extension,
                originalUrl = url,
                thumbnailUrl = url,
                aspectRatio = 1f,
            )
        }

        fun asVideoItem(): VideoItem? {
            return if (!extension.lowercase().contains(Regex("""(mp4|webm)"""))) null
            else VideoItem(
                id = id,
                postId = postId,
                extension = extension,
                url = url,
            )
        }
    }

    companion object {
        fun dummy() = FanboxPostDetail(
            id = PostId("123"),
            title = "オリキャラSkeb絵",
            body = Body.Unknown,
            coverImageUrl = null,
            commentCount = 3,
            excerpt = "リクエストありがとうございました！",
            feeRequired = 0,
            hasAdultContent = true,
            imageForShare = "",
            isLiked = false,
            isBookmarked = false,
            isRestricted = false,
            likeCount = 879,
            tags = listOf("オリキャラ", "Skeb絵"),
            updatedDatetime = Instant.parse("2024-01-01T00:00:00"),
            publishedDatetime = Instant.parse("2024-01-01T00:00:00"),
            nextPost = OtherPost(
                id = PostId("456"),
                title = "ゆんゆん",
                publishedDatetime = Instant.parse("2024-01-01T00:00:00"),
            ),
            prevPost = OtherPost(
                id = PostId("789"),
                title = "ポニテこころん",
                publishedDatetime = Instant.parse("2024-01-01T00:00:00"),
            ),
            user = FanboxUser.dummy(),
        )
    }
}
