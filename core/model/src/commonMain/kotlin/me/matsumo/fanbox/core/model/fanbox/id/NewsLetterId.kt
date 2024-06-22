package me.matsumo.fanbox.core.model.fanbox.id

import com.benasher44.uuid.uuid4

data class NewsLetterId(val value: String) {

    val uniqueValue: String = "news-${value}-${uuid4()}"

    override fun toString(): String = value
}
