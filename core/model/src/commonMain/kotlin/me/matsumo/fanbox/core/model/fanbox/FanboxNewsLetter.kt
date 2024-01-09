package me.matsumo.fanbox.core.model.fanbox

import kotlinx.datetime.Instant
import me.matsumo.fanbox.core.model.fanbox.id.NewsLetterId
import kotlinx.datetime.LocalDateTime

data class FanboxNewsLetter(
    val id: NewsLetterId,
    val body: String,
    val createdAt: Instant,
    val creator: FanboxCreator,
    val isRead: Boolean,
)
