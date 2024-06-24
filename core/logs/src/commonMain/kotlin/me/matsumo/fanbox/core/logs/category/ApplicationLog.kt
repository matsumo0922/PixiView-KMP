package me.matsumo.fanbox.core.logs.category

import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put

// This class is automatically generated by generate-log-classes.
sealed class ApplicationLog : LogCategory {

    class Open internal constructor() : ApplicationLog() {
        override val properties: JsonObject = buildJsonObject {
            put("event_category", "application")
            put("event_name", "open")
        }
    }

    class Close internal constructor(
        private val stayTime: Long
    ) : ApplicationLog() {
        override val properties: JsonObject = buildJsonObject {
            put("event_category", "application")
            put("event_name", "close")
            put("stay_time", stayTime)
        }
    }

    companion object {
        // アプリを開いたときのログ
        fun open() = Open()

        // アプリを閉じたときのログ
        fun close(
            stayTime: Long
        ) = Close(stayTime)
    }
}