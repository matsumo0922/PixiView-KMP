package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate

expect fun Instant.format(pattern: String): String

expect fun LocalDate.format(pattern: String): String

expect fun String.format(vararg args: Any?): String

fun Float.toFileSizeString(): String {
    val mega = 1024f * 1024f
    val giga = mega * 1024f

    return when {
        this > giga -> "%.2f GB".format()
        this > mega -> "%.2f MB".format()
        else -> "%.2f KB".format()
    }
}
