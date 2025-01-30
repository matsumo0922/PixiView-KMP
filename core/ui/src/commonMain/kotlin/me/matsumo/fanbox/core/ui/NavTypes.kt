package me.matsumo.fanbox.core.ui

import androidx.core.bundle.Bundle
import androidx.navigation.NavType
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import kotlin.reflect.typeOf

val PostIdNavType = provideNavType<FanboxPostId>()
val creatorIdNavType = provideNavType<FanboxCreatorId>()

val customNavTypes = mapOf(
    typeOf<FanboxPostId>() to PostIdNavType,
    typeOf<FanboxCreatorId>() to creatorIdNavType,
)

private inline fun <reified T> provideNavType(
    isNullableAllowed: Boolean = false,
) = object : NavType<T>(isNullableAllowed) {

    override fun get(bundle: Bundle, key: String): T? {
        return Json.decodeFromString(bundle.getString(key) ?: return null)
    }

    override fun parseValue(value: String): T {
        return Json.decodeFromString(value)
    }

    override fun put(bundle: Bundle, key: String, value: T) {
        bundle.putString(key, Json.encodeToString(value))
    }

    override fun serializeAsValue(value: T): String {
        return Json.encodeToString(value)
    }
}
