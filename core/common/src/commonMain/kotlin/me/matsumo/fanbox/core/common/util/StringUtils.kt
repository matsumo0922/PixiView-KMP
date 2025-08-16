package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toStdlibInstant
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
expect fun Instant.format(pattern: String): String

@OptIn(ExperimentalTime::class)
fun kotlinx.datetime.Instant.format(pattern: String): String {
    return toStdlibInstant().format(pattern)
}

expect fun LocalDate.format(pattern: String): String

expect fun String.format(vararg args: Any?): String

expect fun getAvailableLanguageTags(): List<String>

fun adjustLanguageTag(inputTag: String): String? {
    val availableTags = getAvailableLanguageTags()

    if (inputTag in availableTags) {
        return inputTag
    }

    val baseLang = inputTag.substringBefore("-")
    val candidates = availableTags.filter {
        it.substringBefore("-").equals(baseLang, ignoreCase = true)
    }

    if (candidates.isNotEmpty()) {
        val inputRegion = inputTag.substringAfter("-", "")
        if (inputRegion.isNotEmpty()) {
            val regionMatch = candidates.find { candidate ->
                candidate.substringAfter("-", "").equals(inputRegion, ignoreCase = true)
            }

            if (regionMatch != null) {
                return regionMatch
            }
        }

        return candidates.first()
    }

    return null
}

fun Float.toFileSizeString(): String {
    val mega = 1024f * 1024f
    val giga = mega * 1024f

    return when {
        this > giga -> "%.2f GB".format(this / giga)
        this > mega -> "%.2f MB".format(this / mega)
        else -> "%.2f KB".format(this / 1024f)
    }
}
