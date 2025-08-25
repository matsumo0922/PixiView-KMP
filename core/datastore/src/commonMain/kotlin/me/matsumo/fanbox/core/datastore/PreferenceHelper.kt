package me.matsumo.fanbox.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.aakira.napier.Napier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject

interface PreferenceHelper {
    fun create(name: String): DataStore<Preferences>
    fun delete(name: String)
}

@OptIn(ExperimentalSerializationApi::class)
fun <T> Preferences.deserialize(
    formatter: Json,
    serializer: KSerializer<T>,
    defaultValue: T,
): T {
    return try {
        val parsedPrefMap: Map<String, JsonElement> = buildMap {
            for ((k, vAny) in this@deserialize.asMap()) {
                val key = k.name
                val elem: JsonElement = when (val v = vAny) {
                    is String -> {
                        // JSON 文字列（{...} or [...]）は parse、通常文字列は Primitive
                        if (v.startsWith("{") && v.endsWith("}") || v.startsWith("[") && v.endsWith("]")) {
                            formatter.parseToJsonElement(v)
                        } else {
                            JsonPrimitive(v)
                        }
                    }

                    is Int -> JsonPrimitive(v)
                    is Long -> JsonPrimitive(v)
                    is Float -> JsonPrimitive(v)
                    is Double -> JsonPrimitive(v)
                    is Boolean -> JsonPrimitive(v)
                    else -> JsonPrimitive(v.toString())
                }
                put(key, elem)
            }
        }

        val defaultData = formatter.encodeToJsonElement(serializer, defaultValue).jsonObject
        val preferenceData = JsonObject(parsedPrefMap)

        val data = buildJsonObject {
            // デフォルトをベースにしつつ、Prefs を上書き
            for ((k, v) in defaultData) put(k, v)
            for ((k, v) in preferenceData) put(k, v)
        }

        formatter.decodeFromJsonElement(serializer, data)
    } catch (e: Exception) {
        Napier.e("Failed to deserialize.", e)
        defaultValue
    }
}
