package me.matsumo.fanbox.core.logs.logger

import io.github.aakira.napier.Napier
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.logs.category.LogCategory
import me.matsumo.fanbox.core.model.UserData
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

object LogConfigurator: KoinComponent {

    private var filter: LogFilter? = null
    private val sender: LogSender by inject()

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

        Napier.d(formatter.encodeToString(JsonObject.serializer(), log))

        sender.sendLog(log)
    }

    fun configure(
        pixiViewConfig: PixiViewConfig,
        userData: UserData,
    ) {
        filter = LogFilter(pixiViewConfig, userData, "PixiView")
    }
}

fun LogCategory.send() = LogConfigurator.send(this)
