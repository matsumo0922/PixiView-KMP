package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toNSDate
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSDateFormatter

actual fun Instant.format(pattern: String): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = pattern

    return dateFormatter.stringFromDate(toNSDate())
}

actual fun LocalDate.format(pattern: String): String {
    val dateFormatter = NSDateFormatter()
    dateFormatter.dateFormat = pattern

    return dateFormatter.stringFromDate(toNSDateComponents().date!!)
}
