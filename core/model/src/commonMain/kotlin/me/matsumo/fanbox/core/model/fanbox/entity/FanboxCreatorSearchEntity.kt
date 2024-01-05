package me.matsumo.fanbox.core.model.fanbox.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxCreatorSearchEntity(
    @SerialName("body")
    val body: Body,
) {
    @Serializable
    data class Body(
        @SerialName("count")
        val count: Int,
        @SerialName("creators")
        val creators: List<FanboxCreatorEntity.Body>,
        @SerialName("nextPage")
        val nextPage: Int?,
    )
}
