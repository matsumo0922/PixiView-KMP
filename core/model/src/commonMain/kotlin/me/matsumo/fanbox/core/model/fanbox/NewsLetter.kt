package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** ニュースレター。 */
@Immutable
@OptIn(ExperimentalTime::class)
data class NewsLetter(
    val id: NewsLetterId,
    val body: String,
    val createdAt: Instant,
    val creator: Creator,
    val isRead: Boolean,
)
