package core.model.fanbox

import core.model.fanbox.id.NewsLetterId
import kotlinx.datetime.LocalDateTime

data class FanboxNewsLetter(
    val id: NewsLetterId,
    val body: String,
    val createdAt: LocalDateTime,
    val creator: FanboxCreator,
    val isRead: Boolean,
)
