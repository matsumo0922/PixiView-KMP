package me.matsumo.fanbox.core.logs.logger

import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.logs.category.LogCategory
import me.matsumo.fanbox.core.model.UserData

object LogConfigurator {

    private var filter: LogFilter? = null

    private val formatter = Json {
        isLenient = true
        prettyPrint = true
    }

    fun send(logCategory: LogCategory) {
        requireNotNull(filter) { "LogConfigurator is not configured" }

        val log = buildJsonObject {
            for (key in logCategory.properties.keys) {
                put(key, logCategory.properties[key]!!)
            }

            filter?.applyFilter(this)
        }

        val json = formatter.encodeToString(JsonObject.serializer(), log)

        Napier.d(json)
    }

    fun configure(
        pixiViewConfig: PixiViewConfig,
        userData: UserData,
    ) {
        filter = LogFilter(pixiViewConfig, userData, "PixiView")
    }
}

fun LogCategory.send() = LogConfigurator.send(this)
