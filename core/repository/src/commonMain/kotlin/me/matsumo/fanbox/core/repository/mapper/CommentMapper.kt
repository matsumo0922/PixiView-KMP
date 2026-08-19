package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.Comment
import me.matsumo.fankt.fanbox.domain.model.FanboxComment
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun FanboxComment.toComment(): Comment {
    return Comment(
        body = body,
        createdDatetime = createdDatetime,
        id = id.toCommentId(),
        isLiked = isLiked,
        isOwn = isOwn,
        likeCount = likeCount,
        parentCommentId = parentCommentId.toCommentId(),
        rootCommentId = rootCommentId.toCommentId(),
        replies = replies.map { it.toComment() },
        user = user?.toUser(),
    )
}
