package me.matsumo.fanbox.core.logs.logger

import kotlinx.serialization.json.JsonObject
import me.matsumo.fanbox.core.model.Setting

interface LogSender {
    fun init(setting: Setting)
    fun sendLog(log: JsonObject)
}
