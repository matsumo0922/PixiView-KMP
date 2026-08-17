package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 投稿へのコメント。 */
@Immutable
@OptIn(ExperimentalTime::class)
data class Comment(
    val body: String,
    val createdDatetime: Instant,
    val id: CommentId,
    val isLiked: Boolean,
    val isOwn: Boolean,
    val likeCount: Int,
    val parentCommentId: CommentId,
    val rootCommentId: CommentId,
    val replies: List<Comment>,
    val user: User?,
)
