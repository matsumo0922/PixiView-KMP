package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.repository.mapper.toFanboxPost
import me.matsumo.fanbox.core.repository.mapper.toPost
import me.matsumo.fankt.fanbox.domain.model.FanboxCover
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxUser
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** [FanboxPost] と [me.matsumo.fanbox.core.model.fanbox.Post] の相互変換を検証するテスト。 */
@OptIn(ExperimentalTime::class)
class PostMapperTest {

    private val fanboxPost = FanboxPost(
        id = FanboxPostId("post-1"),
        title = "title",
        cover = FanboxCover(url = "https://example.com/cover.png", type = "cover"),
        user = FanboxUser(
            userId = FanboxUserId(100),
            creatorId = FanboxCreatorId("creator-1"),
            name = "creator name",
            iconUrl = "https://example.com/icon.png",
        ),
        excerpt = "excerpt",
        feeRequired = 500,
        hasAdultContent = true,
        isLiked = true,
        isRestricted = false,
        likeCount = 10,
        commentCount = 3,
        tags = listOf("tag1", "tag2"),
        publishedDatetime = Instant.parse("2024-01-01T00:00:00Z"),
        updatedDatetime = Instant.parse("2024-01-02T00:00:00Z"),
    )

    /** 全フィールドが値を保ったまま変換されることを確認する。値が欠けていても検出できないため。 */
    @Test
    fun postConversionPreservesAllFields() {
        val post = fanboxPost.toPost()

        assertEquals(fanboxPost.id.value, post.id.value)
        assertEquals(fanboxPost.title, post.title)
        assertEquals(fanboxPost.cover?.url, post.cover?.url)
        assertEquals(fanboxPost.cover?.type, post.cover?.type)
        assertEquals(fanboxPost.user?.userId?.value, post.user?.userId?.value)
        assertEquals(fanboxPost.user?.creatorId?.value, post.user?.creatorId?.value)
        assertEquals(fanboxPost.user?.name, post.user?.name)
        assertEquals(fanboxPost.user?.iconUrl, post.user?.iconUrl)
        assertEquals(fanboxPost.excerpt, post.excerpt)
        assertEquals(fanboxPost.feeRequired, post.feeRequired)
        assertEquals(fanboxPost.hasAdultContent, post.hasAdultContent)
        assertEquals(fanboxPost.isLiked, post.isLiked)
        assertEquals(fanboxPost.isRestricted, post.isRestricted)
        assertEquals(fanboxPost.likeCount, post.likeCount)
        assertEquals(fanboxPost.commentCount, post.commentCount)
        assertEquals(fanboxPost.tags, post.tags)
        assertEquals(fanboxPost.publishedDatetime, post.publishedDatetime)
        assertEquals(fanboxPost.updatedDatetime, post.updatedDatetime)
    }

    /** fankt → app → fankt の往復で元の値へ戻ることを確認する。永続化・再送信の前提となるため。 */
    @Test
    fun postRoundTripReturnsOriginalValue() {
        assertEquals(fanboxPost, fanboxPost.toPost().toFanboxPost())
    }

    /** null 許容フィールドが null のままでも安全に変換できることを確認する。 */
    @Test
    fun postConversionHandlesNullableFields() {
        val nullableFanboxPost = fanboxPost.copy(cover = null, user = null)

        val post = nullableFanboxPost.toPost()

        assertEquals(null, post.cover)
        assertEquals(null, post.user)
        assertEquals(nullableFanboxPost, post.toFanboxPost())
    }
}
