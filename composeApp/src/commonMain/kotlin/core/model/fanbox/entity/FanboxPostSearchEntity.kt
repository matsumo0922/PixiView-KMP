package core.model.fanbox.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxPostSearchEntity(
    @SerialName("body")
    val body: Body,
) {
    @Serializable
    data class Body(
        @SerialName("count")
        val count: Int,
        @SerialName("items")
        val items: List<FanboxPostItemsEntity.Body.Item>,
        @SerialName("nextUrl")
        val nextUrl: String?,
    )
}
