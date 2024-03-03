package me.matsumo.fanbox.core.model.fanbox.id

import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class PostId(val value: String) {

    val uniqueValue: String
        get() = "${value}-${Random.nextInt()}"

    override fun toString(): String = value
}
