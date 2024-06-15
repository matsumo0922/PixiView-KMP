package me.matsumo.fanbox.core.logs.logger

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ParametersBuilder
import com.google.firebase.analytics.logEvent
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.longOrNull

@SuppressLint("MissingPermission")
class LogSenderImpl(context: Context) : LogSender {

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun sendLog(log: JsonObject) {
        val eventName = listOf(
            log["event_category"]?.jsonPrimitive?.content,
            log["event_name"]?.jsonPrimitive?.content,
        ).joinToString("_")

        analytics.logEvent(eventName) {
            for ((key, value) in log) {
                param(key, value)
            }
        }
    }

    private fun ParametersBuilder.param(key: String, value: JsonElement) {
        val longValue = value.jsonPrimitive.longOrNull
        val doubleValue = value.jsonPrimitive.doubleOrNull

        when {
            longValue != null -> param(key, longValue)
            doubleValue != null -> param(key, doubleValue)
            else -> param(key, value.jsonPrimitive.content)
        }
    }
}
