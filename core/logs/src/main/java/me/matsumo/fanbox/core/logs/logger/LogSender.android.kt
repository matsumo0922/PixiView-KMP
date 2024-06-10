package me.matsumo.fanbox.core.logs.logger

import android.annotation.SuppressLint
import android.content.Context
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.logEvent
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonPrimitive

@SuppressLint("MissingPermission")
class LogSenderImpl(
    private val context: Context,
): LogSender {

    private val analytics = FirebaseAnalytics.getInstance(context)

    override fun sendLog(log: JsonObject) {
        val eventName = listOf(log["event_category"], log["event_name"]).joinToString("_")

        analytics.logEvent(eventName) {
            for ((key, value) in log) {
                param(key, )
            }
        }
    }
}
