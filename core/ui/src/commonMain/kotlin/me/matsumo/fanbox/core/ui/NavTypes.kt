package me.matsumo.fanbox.core.ui

import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import kotlin.reflect.typeOf

val PostIdNavType = provideNavType<FanboxPostId>(
    encode = { it.value },
    decode = { FanboxPostId(it) },
)

val creatorIdNavType = provideNavType<FanboxCreatorId>(
    encode = { it.value },
    decode = { FanboxCreatorId(it) },
)

val customNavTypes = mapOf(
    typeOf<FanboxPostId>() to PostIdNavType,
    typeOf<FanboxCreatorId>() to creatorIdNavType,
)

private inline fun <reified T> provideNavType(
    crossinline encode: (T) -> String = { Json.encodeToString(it) },
    crossinline decode: (String) -> T = { Json.decodeFromString(it) },
    isNullableAllowed: Boolean = false,
) = object : NavType<T>(isNullableAllowed) {

    override fun get(bundle: Bundle, key: String): T? {
        return decode(bundle.getString(key) ?: return null)
    }

    override fun parseValue(value: String): T {
        return decode(value)
    }

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, encode(value))
    }

    override fun serializeAsValue(value: T): String {
        return encode(value)
    }
}
