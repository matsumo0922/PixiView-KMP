package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.Cover
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.User
import me.matsumo.fankt.fanbox.domain.model.FanboxCover
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxUser
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun FanboxPost.toPost(): Post {
    return Post(
        id = id.toPostId(),
        title = title,
        cover = cover?.toCover(),
        user = user?.toUser(),
        excerpt = excerpt,
        feeRequired = feeRequired,
        hasAdultContent = hasAdultContent,
        isLiked = isLiked,
        isRestricted = isRestricted,
        likeCount = likeCount,
        commentCount = commentCount,
        tags = tags,
        publishedDatetime = publishedDatetime,
        updatedDatetime = updatedDatetime,
    )
}

@OptIn(ExperimentalTime::class)
fun Post.toFanboxPost(): FanboxPost {
    return FanboxPost(
        id = id.toFanboxPostId(),
        title = title,
        cover = cover?.toFanboxCover(),
        user = user?.toFanboxUser(),
        excerpt = excerpt,
        feeRequired = feeRequired,
        hasAdultContent = hasAdultContent,
        isLiked = isLiked,
        isRestricted = isRestricted,
        likeCount = likeCount,
        commentCount = commentCount,
        tags = tags,
        publishedDatetime = publishedDatetime,
        updatedDatetime = updatedDatetime,
    )
}

private fun Cover.toFanboxCover(): FanboxCover {
    return FanboxCover(
        url = url,
        type = type,
    )
}

private fun User.toFanboxUser(): FanboxUser {
    return FanboxUser(
        userId = userId?.toFanboxUserId(),
        creatorId = creatorId?.toFanboxCreatorId(),
        name = name,
        iconUrl = iconUrl,
    )
}
