package me.matsumo.fanbox.core.model.fanbox

import kotlinx.datetime.Instant
import me.matsumo.fanbox.core.model.fanbox.id.CommentId

data class FanboxComments(
    val items: List<Item>,
    val nextUrl: String?,
) {
    data class Item(
        val body: String,
        val createdDatetime: Instant,
        val id: CommentId,
        val isLiked: Boolean,
        val isOwn: Boolean,
        val likeCount: Int,
        val parentCommentId: CommentId,
        val rootCommentId: CommentId,
        val replies: List<Item>,
        val user: FanboxUser,
    )
}