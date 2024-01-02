package core.model.fanbox

import core.model.fanbox.id.CreatorId
import kotlinx.serialization.Serializable

@Serializable
data class FanboxUser(
    val userId: String,
    val creatorId: CreatorId,
    val name: String,
    val iconUrl: String?,
) {
    companion object {
        fun dummy() = FanboxUser(
            userId = "",
            creatorId = CreatorId("island"),
            name = "あいらんど",
            iconUrl = "https://pixiv.pximg.net/c/160x160_90_a2_g5/fanbox/public/images/user/24164271/icon/3sMCyeX4owee4LwXhwpyMkpv.jpeg",
        )
    }
}
