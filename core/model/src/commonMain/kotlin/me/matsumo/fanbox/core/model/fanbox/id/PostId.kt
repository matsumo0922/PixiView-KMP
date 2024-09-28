package me.matsumo.fanbox.core.model.fanbox.id

import kotlinx.serialization.Serializable
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

@Serializable
data class PostId(val value: String) {

    @OptIn(ExperimentalUuidApi::class)
    val uniqueValue: String = "post-$value-${Uuid.random()}"

    override fun toString(): String = value
}
