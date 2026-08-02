package me.matsumo.fanbox.core.datastore

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonNull
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import me.matsumo.fankt.fanbox.domain.model.FanboxPost

/**
 * 保存済みブックマークの JSON を、fankt 0.1.0 の serializer が読める形式へ正規化する。
 *
 * fankt 0.1.0 で ID 型が `data class` から `@JvmInline value class` へ変わったため、
 * `FanboxPostId` / `FanboxUserId` / `FanboxCreatorId` の JSON 表現が object から primitive になった。
 * 0.0.21 以前が書き込んだ JSON は 0.1.0 の serializer では読めず、`BookmarkDataStore.get()` は
 * デコード失敗を握りつぶすため、変換しなければブックマークが黙って全件消える。
 *
 * 変換対象は `id` / `user.userId` / `user.creatorId` の 3 箇所に限られる。`FanboxPost` の他の
 * フィールドは新旧で JSON 表現が変わらず、`publishedDatetime` / `updatedDatetime` はどちらも
 * ISO-8601 文字列である。
 */

/** 正規化の結果。 */
internal sealed interface BookmarkJsonNormalization {

    /** 3 箇所とも既に primitive で、変換の必要がなかった。 */
    data class AlreadyCurrent(val json: String) : BookmarkJsonNormalization

    /** 1 箇所以上を primitive へ展開した。[json] は展開後の JSON。 */
    data class Migrated(val json: String) : BookmarkJsonNormalization

    /** JSON object として解析できないか、ID が想定外の形だった。 */
    data object Failure : BookmarkJsonNormalization
}

/**
 * 旧形式の ID を primitive へ展開する。
 *
 * 3 箇所の ID をそれぞれ独立に判定するため、一部だけが旧形式の JSON も正規化できる。
 *
 * この関数が保証するのは ID の shape だけで、[FanboxPost] として妥当かは判定しない。`FanboxPost` の
 * 必須フィールドが欠けた JSON も、ID さえ正規化できれば成功する。妥当性の判定は
 * [migrateBookmarkJson] が担う。
 *
 * `user` と `user` 配下の 2 つの ID はいずれも nullable で、既定の [Json] は null を明示的に書き出す。
 * `"user":null` と `"userId":null` は正常な値としてそのまま保持する。フィールドの欠落も、null の
 * 補完はせず入力のまま残す。
 *
 * JSON object として解析できない入力、および ID が object でも primitive でも null でもない入力に
 * 対して [BookmarkJsonNormalization.Failure] を返す。
 */
internal fun normalizeBookmarkIdJson(json: String): BookmarkJsonNormalization {
    val root = runCatching { Json.parseToJsonElement(json) }.getOrNull() as? JsonObject
        ?: return BookmarkJsonNormalization.Failure

    val id = normalizeId(root["id"]) ?: return BookmarkJsonNormalization.Failure
    val user = normalizeUser(root["user"]) ?: return BookmarkJsonNormalization.Failure

    if (!id.changed && !user.changed) {
        return BookmarkJsonNormalization.AlreadyCurrent(json)
    }

    val normalized = JsonObject(
        buildMap {
            putAll(root)
            id.element?.let { put("id", it) }
            user.element?.let { put("user", it) }
        },
    )

    return BookmarkJsonNormalization.Migrated(Json.encodeToString(JsonElement.serializer(), normalized))
}

/**
 * 正規化した要素と、入力から変化したかどうか。
 *
 * [element] が null のとき、その要素は入力に存在しなかったことを表す。呼び出し側はキーを追加しない。
 */
private data class Normalized(val element: JsonElement?, val changed: Boolean)

/**
 * `user` を正規化する。`user` の欠落と [JsonNull] は変換せずそのまま扱う。
 *
 * `user` が object でも null でもない場合、および配下の ID を正規化できない場合に null を返す。
 */
private fun normalizeUser(user: JsonElement?): Normalized? {
    if (user == null || user is JsonNull) return Normalized(user, changed = false)

    val userObject = user as? JsonObject ?: return null
    val userId = normalizeId(userObject["userId"]) ?: return null
    val creatorId = normalizeId(userObject["creatorId"]) ?: return null

    if (!userId.changed && !creatorId.changed) {
        return Normalized(user, changed = false)
    }

    val normalized = JsonObject(
        buildMap {
            putAll(userObject)
            userId.element?.let { put("userId", it) }
            creatorId.element?.let { put("creatorId", it) }
        },
    )

    return Normalized(normalized, changed = true)
}

/**
 * 単一の ID を正規化する。
 *
 * 旧形式の `{"value": ...}` は内側の primitive へ展開する。既に primitive であるか [JsonNull] である
 * 場合、およびフィールド自体が存在しない場合は変換しない。
 *
 * それ以外の形（`value` を持たない object、`value` が primitive でない object、配列）に対して null を
 * 返す。
 */
private fun normalizeId(id: JsonElement?): Normalized? {
    return when (id) {
        null -> Normalized(null, changed = false)
        is JsonNull -> Normalized(id, changed = false)
        is JsonPrimitive -> Normalized(id, changed = false)
        is JsonObject -> (id["value"] as? JsonPrimitive)?.let { Normalized(it, changed = true) }
        else -> null
    }
}

/**
 * 保存済みの JSON を [FanboxPost] へ復元する。旧形式は新形式へ正規化してから復元する。
 *
 * 正規化できない JSON、および正規化しても [FanboxPost] として復元できない JSON に対して null を返す。
 * 失敗したエントリの記録は呼び出し側が行う。
 *
 * fankt が 0.1.0 未満の間、この関数は正常な入力に対しても常に null を返す。0.0.21 の serializer は
 * ID を object として読むため、正規化後の primitive を受け付けないためである。0.1.0 へ更新して初めて
 * 意図した動作になる。
 */
internal fun migrateBookmarkJson(json: String): FanboxPost? {
    val normalized = when (val normalization = normalizeBookmarkIdJson(json)) {
        is BookmarkJsonNormalization.AlreadyCurrent -> normalization.json
        is BookmarkJsonNormalization.Migrated -> normalization.json
        BookmarkJsonNormalization.Failure -> return null
    }

    return runCatching { Json.decodeFromString(FanboxPost.serializer(), normalized) }.getOrNull()
}
