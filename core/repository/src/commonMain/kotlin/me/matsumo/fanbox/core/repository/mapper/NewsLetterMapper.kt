package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.NewsLetter
import me.matsumo.fankt.fanbox.domain.model.FanboxNewsLetter
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun FanboxNewsLetter.toNewsLetter(): NewsLetter {
    return NewsLetter(
        id = id.toNewsLetterId(),
        body = body,
        createdAt = createdAt,
        creator = creator.toCreator(),
        isRead = isRead,
    )
}
