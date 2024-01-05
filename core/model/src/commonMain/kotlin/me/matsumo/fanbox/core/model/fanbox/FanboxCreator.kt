package me.matsumo.fanbox.core.model.fanbox

import me.matsumo.fanbox.core.model.fanbox.id.CreatorId

data class FanboxCreator(
    val creatorId: CreatorId?,
    val user: FanboxUser,
)
