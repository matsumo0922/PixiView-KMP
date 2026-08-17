package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.Bell
import me.matsumo.fankt.fanbox.domain.model.FanboxBell
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun FanboxBell.toBell(): Bell {
    return when (this) {
        is FanboxBell.Comment -> Bell.Comment(
            id = id.toCommentId(),
            notifiedDatetime = notifiedDatetime,
            comment = comment,
            isRootComment = isRootComment,
            creatorId = creatorId.toCreatorId(),
            postId = postId.toPostId(),
            postTitle = postTitle,
            userName = userName,
            userProfileIconUrl = userProfileIconUrl,
        )

        is FanboxBell.Like -> Bell.Like(
            id = id,
            notifiedDatetime = notifiedDatetime,
            comment = comment,
            creatorId = creatorId.toCreatorId(),
            postId = postId.toPostId(),
            count = count,
        )

        is FanboxBell.PostPublished -> Bell.PostPublished(
            id = id.toPostId(),
            notifiedDatetime = notifiedDatetime,
            post = post.toPost(),
        )
    }
}
