package me.matsumo.fanbox.core.datastore

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * 保存済みブックマーク JSON の ID 正規化を検証するテスト。
 *
 * fixture は fankt v0.0.21 と 0.1.0 でそれぞれ実際に `Json.encodeToString` を実行して得た出力に基づく。
 */
class BookmarkJsonMigrationTest {

    private val legacyJson = """
        {"id":{"value":"1234567","uniqueValue":"post-1234567-fe1d64ca-0564-4001-a9a0-c7aa2a084aca"},"title":"sample title","cover":{"url":"https://example.com/cover.jpg","type":"cover_image"},"user":{"userId":{"value":999},"creatorId":{"value":"creator-abc"},"name":"sample user","iconUrl":"https://example.com/icon.jpg"},"excerpt":"sample excerpt","feeRequired":500,"hasAdultContent":true,"isLiked":false,"isRestricted":false,"likeCount":12,"commentCount":3,"tags":["tagA","tagB"],"publishedDatetime":"2026-01-02T03:04:05Z","updatedDatetime":"2026-01-03T04:05:06Z"}
    """.trimIndent()

    private val currentJson = """
        {"id":"1234567","title":"sample title","cover":{"url":"https://example.com/cover.jpg","type":"cover_image"},"user":{"userId":999,"creatorId":"creator-abc","name":"sample user","iconUrl":"https://example.com/icon.jpg"},"excerpt":"sample excerpt","feeRequired":500,"hasAdultContent":true,"isLiked":false,"isRestricted":false,"likeCount":12,"commentCount":3,"tags":["tagA","tagB"],"publishedDatetime":"2026-01-02T03:04:05Z","updatedDatetime":"2026-01-03T04:05:06Z"}
    """.trimIndent()

    @Test
    fun legacyJsonUnwrapsAllThreeIds() {
        val result = normalizeBookmarkIdJson(legacyJson)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val root = Json.parseToJsonElement(result.json).jsonObject
        assertEquals("1234567", root.getValue("id").jsonPrimitive.content)

        val user = root.getValue("user").jsonObject
        assertEquals(999L, user.getValue("userId").jsonPrimitive.content.toLong())
        assertEquals("creator-abc", user.getValue("creatorId").jsonPrimitive.content)
    }

    @Test
    fun legacyUserIdKeepsNumericType() {
        val result = normalizeBookmarkIdJson(legacyJson)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val userId = Json.parseToJsonElement(result.json).jsonObject
            .getValue("user").jsonObject
            .getValue("userId")

        assertTrue((userId as JsonPrimitive).isString.not())
    }

    @Test
    fun legacyJsonBecomesByteIdenticalToCurrentFormat() {
        val result = normalizeBookmarkIdJson(legacyJson)

        assertIs<BookmarkJsonNormalization.Migrated>(result)
        assertEquals(currentJson, result.json)
    }

    @Test
    fun currentJsonIsReportedAsAlreadyCurrent() {
        val result = normalizeBookmarkIdJson(currentJson)

        assertIs<BookmarkJsonNormalization.AlreadyCurrent>(result)
        assertEquals(currentJson, result.json)
    }

    @Test
    fun nullUserIsPreserved() {
        val json = """{"id":{"value":"1"},"title":"t","cover":null,"user":null,"tags":[]}"""

        val result = normalizeBookmarkIdJson(json)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val root = Json.parseToJsonElement(result.json).jsonObject
        assertEquals("1", root.getValue("id").jsonPrimitive.content)
        assertTrue(root.getValue("user") is kotlinx.serialization.json.JsonNull)
    }

    @Test
    fun nullIdsInsideUserArePreserved() {
        val json = """{"id":{"value":"1"},"user":{"userId":null,"creatorId":null,"name":"n","iconUrl":null}}"""

        val result = normalizeBookmarkIdJson(json)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val user = Json.parseToJsonElement(result.json).jsonObject.getValue("user").jsonObject
        assertTrue(user.getValue("userId") is kotlinx.serialization.json.JsonNull)
        assertTrue(user.getValue("creatorId") is kotlinx.serialization.json.JsonNull)
        assertEquals("n", user.getValue("name").jsonPrimitive.content)
    }

    @Test
    fun nullUserIdWithLegacyCreatorIdIsMigrated() {
        val json = """{"id":"1","user":{"userId":null,"creatorId":{"value":"creator"},"name":"n"}}"""

        val result = normalizeBookmarkIdJson(json)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val user = Json.parseToJsonElement(result.json).jsonObject.getValue("user").jsonObject
        assertTrue(user.getValue("userId") is kotlinx.serialization.json.JsonNull)
        assertEquals("creator", user.getValue("creatorId").jsonPrimitive.content)
    }

    @Test
    fun legacyUserIdWithNullCreatorIdIsMigrated() {
        val json = """{"id":"1","user":{"userId":{"value":999},"creatorId":null,"name":"n"}}"""

        val result = normalizeBookmarkIdJson(json)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val user = Json.parseToJsonElement(result.json).jsonObject.getValue("user").jsonObject
        assertEquals(999L, user.getValue("userId").jsonPrimitive.content.toLong())
        assertTrue(user.getValue("creatorId") is kotlinx.serialization.json.JsonNull)
    }

    @Test
    fun nullUserWithCurrentRootIdIsAlreadyCurrent() {
        val json = """{"id":"1","user":null}"""

        val result = normalizeBookmarkIdJson(json)

        assertIs<BookmarkJsonNormalization.AlreadyCurrent>(result)
        assertEquals(json, result.json)
    }

    @Test
    fun mixedFormatIsMigratedPerField() {
        val json = """{"id":"1","user":{"userId":{"value":999},"creatorId":"creator","name":"n"}}"""

        val result = normalizeBookmarkIdJson(json)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val root = Json.parseToJsonElement(result.json).jsonObject
        assertEquals("1", root.getValue("id").jsonPrimitive.content)

        val user = root.getValue("user").jsonObject
        assertEquals(999L, user.getValue("userId").jsonPrimitive.content.toLong())
        assertEquals("creator", user.getValue("creatorId").jsonPrimitive.content)
    }

    @Test
    fun missingUserKeyIsNotAdded() {
        val json = """{"id":{"value":"1"},"title":"t"}"""

        val result = normalizeBookmarkIdJson(json)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val root = Json.parseToJsonElement(result.json).jsonObject
        assertTrue(!root.containsKey("user"))
    }

    @Test
    fun missingIdKeyIsNotAdded() {
        val json = """{"title":"t","user":{"userId":{"value":999},"name":"n"}}"""

        val result = normalizeBookmarkIdJson(json)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val root = Json.parseToJsonElement(result.json).jsonObject
        assertTrue(!root.containsKey("id"))
        assertEquals(999L, root.getValue("user").jsonObject.getValue("userId").jsonPrimitive.content.toLong())
    }

    @Test
    fun unrelatedFieldsArePreserved() {
        val result = normalizeBookmarkIdJson(legacyJson)

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val root = Json.parseToJsonElement(result.json).jsonObject
        assertEquals("2026-01-02T03:04:05Z", root.getValue("publishedDatetime").jsonPrimitive.content)
        assertEquals("2026-01-03T04:05:06Z", root.getValue("updatedDatetime").jsonPrimitive.content)
        assertEquals(500, root.getValue("feeRequired").jsonPrimitive.content.toInt())
        assertEquals(2, root.getValue("tags").let { it as kotlinx.serialization.json.JsonArray }.size)
        assertEquals(
            "https://example.com/cover.jpg",
            root.getValue("cover").jsonObject.getValue("url").jsonPrimitive.content,
        )
    }

    @Test
    fun malformedJsonFails() {
        assertEquals(BookmarkJsonNormalization.Failure, normalizeBookmarkIdJson("not json"))
    }

    @Test
    fun nonObjectRootFails() {
        assertEquals(BookmarkJsonNormalization.Failure, normalizeBookmarkIdJson("""["a"]"""))
        assertEquals(BookmarkJsonNormalization.Failure, normalizeBookmarkIdJson(""""string""""))
    }

    @Test
    fun idObjectWithoutValueFails() {
        assertEquals(BookmarkJsonNormalization.Failure, normalizeBookmarkIdJson("""{"id":{"other":"1"}}"""))
    }

    @Test
    fun idObjectWithNestedObjectValueFails() {
        assertEquals(
            BookmarkJsonNormalization.Failure,
            normalizeBookmarkIdJson("""{"id":{"value":{"nested":"1"}}}"""),
        )
    }

    @Test
    fun idArrayFails() {
        assertEquals(BookmarkJsonNormalization.Failure, normalizeBookmarkIdJson("""{"id":["1"]}"""))
    }

    @Test
    fun nonObjectUserFails() {
        assertEquals(BookmarkJsonNormalization.Failure, normalizeBookmarkIdJson("""{"id":"1","user":"u"}"""))
    }

    @Test
    fun malformedIdInsideUserFails() {
        assertEquals(
            BookmarkJsonNormalization.Failure,
            normalizeBookmarkIdJson("""{"id":"1","user":{"userId":{"other":1},"name":"n"}}"""),
        )
    }

    @Test
    fun malformedCreatorIdFails() {
        assertEquals(
            BookmarkJsonNormalization.Failure,
            normalizeBookmarkIdJson("""{"id":"1","user":{"creatorId":{"other":1},"name":"n"}}"""),
        )
    }

    @Test
    fun idObjectWithNullValueUnwrapsToNull() {
        val result = normalizeBookmarkIdJson("""{"id":{"value":null}}""")

        assertIs<BookmarkJsonNormalization.Migrated>(result)

        val id = Json.parseToJsonElement(result.json).jsonObject.getValue("id")
        assertTrue(id is kotlinx.serialization.json.JsonNull)
    }

    @Test
    fun migrateReturnsNullForMalformedJson() {
        assertNull(migrateBookmarkJson("not json"))
    }

    @Test
    fun migrateReturnsNullWhenRequiredFieldsAreMissing() {
        assertNull(migrateBookmarkJson("""{"id":{"value":"1"}}"""))
    }
}
