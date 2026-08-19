package me.matsumo.fanbox.core.repository

import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.repository.mapper.toCreatorId
import me.matsumo.fanbox.core.repository.mapper.toFanboxPost
import me.matsumo.fanbox.core.repository.mapper.toPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import org.junit.Test
import kotlin.test.assertEquals

/**
 * 置き換え前に保存された内容が、アプリ所有のモデルへ移った後も同じように読めることを検証するテスト。
 *
 * ブックマークは `BookmarkDataStore` が fankt の `FanboxPost` の serializer で JSON を書いており、
 * 保存形式は本変更でも変えていない。アプリ所有の [me.matsumo.fanbox.core.model.fanbox.Post] は
 * 読み出しと書き込みの境界で相互変換される。この往復で値が落ちると、既存ユーザーのブックマークが
 * 静かに壊れる。
 */
class BookmarkPersistenceCompatibilityTest {

    private val storedBookmarkJson = """
        {"id":"post-1","title":"title","cover":{"url":"https://example.com/cover.png","type":"cover"},"user":{"userId":100,"creatorId":"creator-1","name":"creator name","iconUrl":"https://example.com/icon.png"},"excerpt":"excerpt","feeRequired":500,"hasAdultContent":true,"isLiked":true,"isRestricted":false,"likeCount":10,"commentCount":3,"tags":["tag1","tag2"],"publishedDatetime":"2024-01-01T00:00:00Z","updatedDatetime":"2024-01-02T00:00:00Z"}
    """.trimIndent()

    /** 保存済みの JSON がアプリ所有のモデルとして復元できることを確認する。 */
    @Test
    fun storedBookmarkJsonIsReadableAsAppOwnedPost() {
        val storedPost = Json.decodeFromString(FanboxPost.serializer(), storedBookmarkJson)

        val post = storedPost.toPost()

        assertEquals("post-1", post.id.value)
        assertEquals("title", post.title)
        assertEquals("https://example.com/cover.png", post.cover?.url)
        assertEquals(100L, post.user?.userId?.value)
        assertEquals("creator-1", post.user?.creatorId?.value)
        assertEquals(500, post.feeRequired)
        assertEquals(listOf("tag1", "tag2"), post.tags)
        assertEquals(storedPost.publishedDatetime, post.publishedDatetime)
    }

    /** アプリ所有のモデルから書き戻した JSON が、保存済みの JSON と一致することを確認する。 */
    @Test
    fun writingBackAnAppOwnedPostKeepsTheStoredJsonFormat() {
        val storedPost = Json.decodeFromString(FanboxPost.serializer(), storedBookmarkJson)

        val writtenBackJson = Json.encodeToString(FanboxPost.serializer(), storedPost.toPost().toFanboxPost())

        assertEquals(storedBookmarkJson, writtenBackJson)
    }

    /** 保存済みのブロック済みクリエイターが、アプリ所有の ID として復元できることを確認する。 */
    @Test
    fun storedBlockedCreatorsAreReadableAsAppOwnedIds() {
        val storedCreatorIds = setOf("creator-1", "creator-2")

        val creatorIds = storedCreatorIds.map { FanboxCreatorId(it).toCreatorId() }

        assertEquals(storedCreatorIds, creatorIds.map { it.value }.toSet())
    }
}
