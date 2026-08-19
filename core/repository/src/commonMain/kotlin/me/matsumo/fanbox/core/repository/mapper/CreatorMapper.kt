package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.Creator
import me.matsumo.fankt.fanbox.domain.model.FanboxCreator

fun FanboxCreator.toCreator(): Creator {
    return Creator(
        creatorId = creatorId?.toCreatorId(),
        user = user?.toUser(),
    )
}
