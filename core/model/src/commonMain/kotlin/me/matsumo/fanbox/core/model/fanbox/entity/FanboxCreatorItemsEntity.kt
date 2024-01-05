package me.matsumo.fanbox.core.model.fanbox.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxCreatorItemsEntity(
    @SerialName("body")
    val body: List<FanboxCreatorEntity.Body>,
)
