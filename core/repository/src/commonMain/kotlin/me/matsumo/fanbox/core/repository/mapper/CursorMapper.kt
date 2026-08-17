package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.Cursor
import me.matsumo.fankt.fanbox.domain.FanboxCursor

fun FanboxCursor.toCursor(): Cursor {
    return Cursor(
        firstPublishedDatetime = firstPublishedDatetime,
        maxPublishedDatetime = maxPublishedDatetime,
        firstId = firstId,
        maxId = maxId,
        limit = limit,
    )
}

fun Cursor.toFanboxCursor(): FanboxCursor {
    return FanboxCursor(
        firstPublishedDatetime = firstPublishedDatetime,
        maxPublishedDatetime = maxPublishedDatetime,
        firstId = firstId,
        maxId = maxId,
        limit = limit,
    )
}
