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
): T = try {
    val parsedPreferences = asMap().entries.associate { (preference, value) ->
        preference.name to value.toPreferenceJson(formatter)
    }
    val defaultData = formatter.encodeToJsonElement(serializer, defaultValue).jsonObject
    val data = JsonObject(defaultData + parsedPreferences)

    formatter.decodeFromJsonElement(serializer, data)
} catch (exception: Exception) {
    Napier.e("Failed to deserialize.", exception)
    defaultValue
}

private fun Any.toPreferenceJson(formatter: Json): JsonElement = when (this) {
    is String -> {
        val isJsonObject = startsWith("{") && endsWith("}")
        val isJsonArray = startsWith("[") && endsWith("]")

        if (isJsonObject || isJsonArray) formatter.parseToJsonElement(this) else JsonPrimitive(this)
    }

    is Int -> JsonPrimitive(this)

    is Long -> JsonPrimitive(this)

    is Float -> JsonPrimitive(this)

    is Double -> JsonPrimitive(this)

    is Boolean -> JsonPrimitive(this)

    else -> JsonPrimitive(toString())
}
