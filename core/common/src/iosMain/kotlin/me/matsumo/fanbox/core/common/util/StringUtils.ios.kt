package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toNSDate
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSCalendar
import platform.Foundation.NSDateFormatter
import platform.Foundation.timeIntervalSince1970

actual fun Instant.format(pattern: String): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = pattern

    return dateFormatter.stringFromDate(toNSDate())
}
