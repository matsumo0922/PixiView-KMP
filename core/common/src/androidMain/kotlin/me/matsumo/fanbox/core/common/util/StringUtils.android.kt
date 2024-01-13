package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toJavaLocalDate
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter

actual fun Instant.format(pattern: String): String {
    return toLocalDateTime(TimeZone.currentSystemDefault()).toJavaLocalDateTime().format(DateTimeFormatter.ofPattern(pattern))
}

actual fun LocalDate.format(pattern: String): String {
    return toJavaLocalDate().format(DateTimeFormatter.ofPattern(pattern))
}

actual fun String.format(vararg args: Any?): String {
    return java.lang.String.format(this, *args)
}
