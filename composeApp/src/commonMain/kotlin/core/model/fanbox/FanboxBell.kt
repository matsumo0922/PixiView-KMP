package core.model.fanbox

import core.model.fanbox.id.CommentId
import core.model.fanbox.id.CreatorId
import core.model.fanbox.id.PostId
import kotlinx.datetime.LocalDateTime

sealed interface FanboxBell {
    data class Comment(
        val id: CommentId,
        val notifiedDatetime: LocalDateTime,
        val comment: String,
        val isRootComment: Boolean,
        val creatorId: CreatorId,
        val postId: PostId,
        val postTitle: String,
        val userName: String,
        val userProfileIconUrl: String,
    ) : FanboxBell

    data class Like(
        val id: String,
        val notifiedDatetime: LocalDateTime,
        val comment: String,
        val creatorId: CreatorId,
        val postId: PostId,
        val count: Int,
    ) : FanboxBell

    data class PostPublished(
        val id: PostId,
        val notifiedDatetime: LocalDateTime,
        val post: FanboxPost,
    ) : FanboxBell
}
