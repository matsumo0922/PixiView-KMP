package core.model.fanbox

import core.model.fanbox.id.CreatorId

data class FanboxCreator(
    val creatorId: CreatorId?,
    val user: FanboxUser,
)
