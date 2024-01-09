package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.Instant
import kotlin.time.Duration

expect fun Instant.format(pattern: String): String

fun Instant.saturatingDiff(instant: Instant): Duration = when {
    this == instant -> Duration.ZERO
    else -> this - instant
}
