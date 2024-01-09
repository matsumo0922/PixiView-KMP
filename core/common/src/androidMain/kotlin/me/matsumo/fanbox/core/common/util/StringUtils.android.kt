package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.Instant
import kotlinx.datetime.toJavaInstant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale


actual fun Instant.format(pattern: String): String {
    return DateTimeFormatter.ofPattern(pattern)
        .withLocale(Locale.getDefault())
        .withZone(ZoneId.systemDefault())
        .format(this.toJavaInstant())
}
