package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.LocalDate
import kotlinx.datetime.toNSDate
import kotlinx.datetime.toNSDateComponents
import platform.Foundation.NSDateFormatter
import platform.Foundation.NSLocale
import platform.Foundation.NSString
import platform.Foundation.availableLocaleIdentifiers
import platform.Foundation.stringWithFormat
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

@OptIn(ExperimentalTime::class)
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

actual fun String.format(vararg args: Any?): String {
    var returnString = ""
    val regEx = "%[\\d|.]*[sdf]|[%]".toRegex()

    val singleFormats = regEx.findAll(this).map { it.groupValues.first() }.toList()
    val newStrings = this.split(regEx)

    for (i in 0 until args.count()) {
        val arg = args[i]

        returnString += when (arg) {
            is Double -> NSString.stringWithFormat(newStrings[i] + singleFormats[i], args[i] as Double)
            is Float -> NSString.stringWithFormat(newStrings[i] + singleFormats[i], (args[i] as Float).toDouble())
            is Long -> NSString.stringWithFormat(newStrings[i] + singleFormats[i], args[i] as Long)
            is Int -> NSString.stringWithFormat(newStrings[i] + singleFormats[i], args[i] as Int)
            else -> NSString.stringWithFormat(newStrings[i] + "%@", args[i])
        }
    }

    if (newStrings.count() > args.count()) {
        returnString += newStrings.last()
    }

    return returnString
}

actual fun getAvailableLanguageTags(): List<String> {
    return NSLocale.availableLocaleIdentifiers().map { localeId ->
        when (localeId) {
            is String -> localeId
            is NSString -> localeId.toString()
            else -> localeId.toString()
        }
    }
}
