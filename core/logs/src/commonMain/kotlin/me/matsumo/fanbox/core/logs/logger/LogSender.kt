package me.matsumo.fanbox.core.logs.logger

import kotlinx.serialization.json.JsonObject
import me.matsumo.fanbox.core.model.UserData

interface LogSender {
    fun init(userData: UserData)
    fun sendLog(log: JsonObject)
}
