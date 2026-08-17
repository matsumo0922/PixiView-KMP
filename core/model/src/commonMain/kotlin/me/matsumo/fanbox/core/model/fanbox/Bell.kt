package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** ベル通知。 */
@Immutable
sealed interface Bell {
    /** コメント通知。 */
    @Immutable
    @OptIn(ExperimentalTime::class)
    data class Comment(
        val id: CommentId,
        val notifiedDatetime: Instant,
        val comment: String,
        val isRootComment: Boolean,
        val creatorId: CreatorId,
        val postId: PostId,
        val postTitle: String,
        val userName: String,
        val userProfileIconUrl: String,
    ) : Bell

    /** いいね通知。 */
    @Immutable
    @OptIn(ExperimentalTime::class)
    data class Like(
        val id: String,
        val notifiedDatetime: Instant,
        val comment: String,
        val creatorId: CreatorId,
        val postId: PostId,
        val count: Int,
    ) : Bell

    /** 新着投稿通知。 */
    @Immutable
    @OptIn(ExperimentalTime::class)
    data class PostPublished(
        val id: PostId,
        val notifiedDatetime: Instant,
        val post: Post,
    ) : Bell
}
