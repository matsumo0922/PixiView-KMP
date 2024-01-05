package me.matsumo.fanbox.core.model.fanbox.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxCreatorTagsEntity(
    @SerialName("body")
    val body: List<Body>,
) {
    @Serializable
    data class Body(
        @SerialName("count")
        val count: Int,
        @SerialName("coverImageUrl")
        val coverImageUrl: String?,
        @SerialName("tag")
        val tag: String,
    )
}
