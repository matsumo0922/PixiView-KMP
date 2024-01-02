package core.model.fanbox.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxTagsEntity(
    @SerialName("body")
    val body: List<Body>,
) {
    @Serializable
    data class Body(
        @SerialName("count")
        val count: Int,
        @SerialName("value")
        val value: String,
    )
}
