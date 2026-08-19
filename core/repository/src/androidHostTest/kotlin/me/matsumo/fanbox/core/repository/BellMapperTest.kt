package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.model.fanbox.Bell
import me.matsumo.fanbox.core.repository.mapper.toBell
import me.matsumo.fankt.fanbox.domain.model.FanboxBell
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCommentId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * [FanboxBell] からアプリ所有の [Bell] への変換を検証するテスト。
 *
 * 3 つの subtype を分岐で振り分けており、[FanboxBell.Comment] は同じ型の文字列を 4 つ並べて持つ。
 * 取り違えても型が合うためコンパイルでは捕まらない。
 */
@OptIn(ExperimentalTime::class)
class BellMapperTest {

    private val notifiedDatetime = Instant.parse("2024-03-01T00:00:00Z")

    /** コメント通知の全フィールドが写ることを確認する。 */
    @Test
    fun commentBellConversionPreservesAllFields() {
        val fanboxBell = FanboxBell.Comment(
            id = FanboxCommentId("comment-1"),
            notifiedDatetime = notifiedDatetime,
            comment = "comment body",
            isRootComment = true,
            creatorId = FanboxCreatorId("creator-1"),
            postId = FanboxPostId("post-1"),
            postTitle = "post title",
            userName = "user name",
            userProfileIconUrl = "https://example.com/icon.png",
        )

        val bell = assertIs<Bell.Comment>(fanboxBell.toBell())

        assertEquals("comment-1", bell.id.value)
        assertEquals(notifiedDatetime, bell.notifiedDatetime)
        assertEquals("comment body", bell.comment)
        assertEquals(true, bell.isRootComment)
        assertEquals("creator-1", bell.creatorId.value)
        assertEquals("post-1", bell.postId.value)
        assertEquals("post title", bell.postTitle)
        assertEquals("user name", bell.userName)
        assertEquals("https://example.com/icon.png", bell.userProfileIconUrl)
    }

    /** いいね通知の全フィールドが写ることを確認する。 */
    @Test
    fun likeBellConversionPreservesAllFields() {
        val fanboxBell = FanboxBell.Like(
            id = "like-1",
            notifiedDatetime = notifiedDatetime,
            comment = "like comment",
            creatorId = FanboxCreatorId("creator-1"),
            postId = FanboxPostId("post-1"),
            count = 7,
        )

        val bell = assertIs<Bell.Like>(fanboxBell.toBell())

        assertEquals("like-1", bell.id)
        assertEquals(notifiedDatetime, bell.notifiedDatetime)
        assertEquals("like comment", bell.comment)
        assertEquals("creator-1", bell.creatorId.value)
        assertEquals("post-1", bell.postId.value)
        assertEquals(7, bell.count)
    }

    /** 投稿公開通知が、入れ子の投稿ごと写ることを確認する。 */
    @Test
    fun postPublishedBellConversionPreservesTheNestedPost() {
        val fanboxPost = FanboxPost(
            id = FanboxPostId("post-1"),
            title = "title",
            cover = null,
            user = null,
            excerpt = "excerpt",
            feeRequired = 100,
            hasAdultContent = false,
            isLiked = false,
            isRestricted = true,
            likeCount = 1,
            commentCount = 2,
            tags = listOf("tag"),
            publishedDatetime = notifiedDatetime,
            updatedDatetime = notifiedDatetime,
        )
        val fanboxBell = FanboxBell.PostPublished(
            id = FanboxPostId("post-1"),
            notifiedDatetime = notifiedDatetime,
            post = fanboxPost,
        )

        val bell = assertIs<Bell.PostPublished>(fanboxBell.toBell())

        assertEquals("post-1", bell.id.value)
        assertEquals(notifiedDatetime, bell.notifiedDatetime)
        assertEquals("title", bell.post.title)
        assertEquals(100, bell.post.feeRequired)
        assertEquals(listOf("tag"), bell.post.tags)
    }
}
