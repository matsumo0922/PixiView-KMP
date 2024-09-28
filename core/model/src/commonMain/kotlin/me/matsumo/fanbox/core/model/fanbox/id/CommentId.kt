package me.matsumo.fanbox.core.model.fanbox.id

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class CommentId(val value: String) {

    @OptIn(ExperimentalUuidApi::class)
    val uniqueValue: String = "comment-$value-${Uuid.random()}"

    override fun toString(): String = value
}
