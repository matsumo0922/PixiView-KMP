package me.matsumo.fanbox.core.model.fanbox.id

import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid

data class NewsLetterId(val value: String) {

    @OptIn(ExperimentalUuidApi::class)
    val uniqueValue: String = "news-$value-${Uuid.random()}"

    override fun toString(): String = value
}
