package me.matsumo.fanbox.core.logs.logger

import kotlinx.serialization.json.JsonObject

interface LogSender {
    fun sendLog(log: JsonObject)
}
