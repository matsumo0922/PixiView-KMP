package me.matsumo.fanbox.core.model.fanbox.id

import com.benasher44.uuid.Uuid
import com.benasher44.uuid.uuid4
import kotlinx.serialization.Serializable
import kotlin.random.Random

@Serializable
data class PostId(val value: String) {

    val uniqueValue: String = "${value}-${uuid4()}"

    override fun toString(): String = value
}
