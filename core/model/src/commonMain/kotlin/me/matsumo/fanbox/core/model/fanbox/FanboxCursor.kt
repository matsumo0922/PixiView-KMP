package me.matsumo.fanbox.core.model.fanbox

data class FanboxCursor(
    val maxPublishedDatetime: String,
    val maxId: String,
    val limit: Int?,
)
