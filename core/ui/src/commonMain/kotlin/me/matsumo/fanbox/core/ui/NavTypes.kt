package me.matsumo.fanbox.core.ui

import androidx.navigation.NavType
import androidx.savedstate.SavedState
import androidx.savedstate.read
import androidx.savedstate.write
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents
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

val simpleAlertContentsNavType = provideNavType<SimpleAlertContents>(
    encode = { Json.encodeToString(it) },
    decode = { Json.decodeFromString(it) },
)

val postDetailPagingTypeNavType = provideNavType<Destination.PostDetail.PagingType>(
    encode = { it.name },
    decode = { Destination.PostDetail.PagingType.valueOf(it) },
)

val customNavTypes = mapOf(
    typeOf<FanboxPostId>() to PostIdNavType,
    typeOf<FanboxCreatorId>() to creatorIdNavType,
    typeOf<SimpleAlertContents>() to simpleAlertContentsNavType,
    typeOf<Destination.PostDetail.PagingType>() to postDetailPagingTypeNavType,
)

private inline fun <reified T> provideNavType(
    crossinline encode: (T) -> String = { Json.encodeToString(it) },
    crossinline decode: (String) -> T = { Json.decodeFromString(it) },
    isNullableAllowed: Boolean = false,
) = object : NavType<T>(isNullableAllowed) {

    override fun get(bundle: SavedState, key: String): T? {
        return decode(bundle.read { getStringOrNull(key) } ?: return null)
    }

    override fun parseValue(value: String): T {
        return decode(value)
    }

    override fun put(bundle: SavedState, key: String, value: T) {
        bundle.write { putString(key, encode(value)) }
    }

    override fun serializeAsValue(value: T): String {
        return encode(value)
    }
}
