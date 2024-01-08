package me.matsumo.fanbox.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import io.github.aakira.napier.Napier
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializer
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonUnquotedLiteral

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
        val map = this.asMap().map { it.key.name to JsonUnquotedLiteral(it.value.toString()) }.toMap()
        val data = JsonObject(map)

        formatter.decodeFromJsonElement(serializer, data)
    } catch (e: Exception) {
        Napier.e("Failed to deserialize.")
        defaultValue
    }
}
