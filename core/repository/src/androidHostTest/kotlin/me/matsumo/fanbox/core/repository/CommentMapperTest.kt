package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.repository.mapper.toComment
import me.matsumo.fankt.fanbox.domain.model.FanboxComment
import me.matsumo.fankt.fanbox.domain.model.FanboxUser
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCommentId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * [FanboxComment] からアプリ所有のコメントへの変換を検証するテスト。
 *
 * 返信を再帰的にたどるうえ、[FanboxComment.parentCommentId] と [FanboxComment.rootCommentId] が
 * 同じ型である。取り違えても型が合うためコンパイルでは捕まらない。
 */
@OptIn(ExperimentalTime::class)
class CommentMapperTest {

    private val createdDatetime = Instant.parse("2024-03-01T00:00:00Z")

    private fun comment(
        id: String,
        body: String,
        replies: List<FanboxComment> = emptyList(),
    ) = FanboxComment(
        body = body,
        createdDatetime = createdDatetime,
        id = FanboxCommentId(id),
        isLiked = true,
        isOwn = false,
        likeCount = 3,
        parentCommentId = FanboxCommentId("parent-$id"),
        rootCommentId = FanboxCommentId("root-$id"),
        replies = replies,
        user = FanboxUser(
            userId = FanboxUserId(200),
            creatorId = FanboxCreatorId("creator-1"),
            name = "user name",
            iconUrl = "https://example.com/icon.png",
        ),
    )

    /** 全フィールドが写ることを確認する。親とルートの ID を取り違えないことを含む。 */
    @Test
    fun commentConversionPreservesAllFields() {
        val fanboxComment = comment(id = "comment-1", body = "body")

        val converted = fanboxComment.toComment()

        assertEquals("body", converted.body)
        assertEquals(createdDatetime, converted.createdDatetime)
        assertEquals("comment-1", converted.id.value)
        assertEquals(true, converted.isLiked)
        assertEquals(false, converted.isOwn)
        assertEquals(3, converted.likeCount)
        assertEquals("parent-comment-1", converted.parentCommentId.value)
        assertEquals("root-comment-1", converted.rootCommentId.value)
        assertEquals(200L, converted.user?.userId?.value)
        assertEquals("user name", converted.user?.name)
    }

    /** 入れ子の返信が深さを保ったまま写ることを確認する。 */
    @Test
    fun nestedRepliesAreConvertedRecursively() {
        val fanboxComment = comment(
            id = "comment-1",
            body = "root body",
            replies = listOf(
                comment(
                    id = "comment-2",
                    body = "reply body",
                    replies = listOf(comment(id = "comment-3", body = "nested reply body")),
                ),
            ),
        )

        val converted = fanboxComment.toComment()

        assertEquals("reply body", converted.replies.single().body)
        assertEquals("comment-2", converted.replies.single().id.value)
        assertEquals("nested reply body", converted.replies.single().replies.single().body)
        assertEquals("comment-3", converted.replies.single().replies.single().id.value)
    }
}
