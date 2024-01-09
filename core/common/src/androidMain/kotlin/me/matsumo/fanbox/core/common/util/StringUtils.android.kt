package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.format.DateTimeFormatter


actual fun Instant.format(pattern: String): String {
    return DateTimeFormatter.ofPattern(pattern).format(this.toJavaInstant())
}
