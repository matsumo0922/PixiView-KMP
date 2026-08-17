package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.User
import me.matsumo.fankt.fanbox.domain.model.FanboxUser

fun FanboxUser.toUser(): User {
    return User(
        userId = userId?.toUserId(),
        creatorId = creatorId?.toCreatorId(),
        name = name,
        iconUrl = iconUrl,
    )
}
