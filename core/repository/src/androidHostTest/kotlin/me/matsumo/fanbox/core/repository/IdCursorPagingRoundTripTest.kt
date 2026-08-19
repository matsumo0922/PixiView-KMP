package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.model.fanbox.CommentId
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.UserId
import me.matsumo.fanbox.core.repository.mapper.toCommentId
import me.matsumo.fanbox.core.repository.mapper.toCreatorId
import me.matsumo.fanbox.core.repository.mapper.toCursor
import me.matsumo.fanbox.core.repository.mapper.toFanboxCommentId
import me.matsumo.fanbox.core.repository.mapper.toFanboxCreatorId
import me.matsumo.fanbox.core.repository.mapper.toFanboxCursor
import me.matsumo.fanbox.core.repository.mapper.toFanboxPostId
import me.matsumo.fanbox.core.repository.mapper.toFanboxUserId
import me.matsumo.fanbox.core.repository.mapper.toPageCursorInfo
import me.matsumo.fanbox.core.repository.mapper.toPostId
import me.matsumo.fanbox.core.repository.mapper.toUserId
import me.matsumo.fankt.fanbox.domain.FanboxCursor
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCommentId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.junit.Test
import kotlin.test.assertEquals
import me.matsumo.fankt.fanbox.domain.PageCursorInfo as FanktPageCursorInfo

/** ID と Cursor の fankt ↔ app 往復変換が元の値へ戻ることを検証するテスト。 */
class IdCursorPagingRoundTripTest {

    @Test
    fun postIdRoundTripReturnsOriginalValue() {
        val original = FanboxPostId("post-1")

        assertEquals(original, original.toPostId().toFanboxPostId())
    }

    @Test
    fun creatorIdRoundTripReturnsOriginalValue() {
        val original = FanboxCreatorId("creator-1")

        assertEquals(original, original.toCreatorId().toFanboxCreatorId())
    }

    @Test
    fun userIdRoundTripReturnsOriginalValue() {
        val original = FanboxUserId(100)

        assertEquals(original, original.toUserId().toFanboxUserId())
    }

    @Test
    fun commentIdRoundTripReturnsOriginalValue() {
        val original = FanboxCommentId("comment-1")

        assertEquals(original, original.toCommentId().toFanboxCommentId())
    }

    /** app 側 ID から往復しても値が戻ることを確認する。app 側で生成した ID を fankt へ送る経路のため。 */
    @Test
    fun appOwnedIdRoundTripReturnsOriginalValue() {
        val postId = PostId("post-1")
        val creatorId = CreatorId("creator-1")
        val userId = UserId(100)
        val commentId = CommentId("comment-1")

        assertEquals(postId, postId.toFanboxPostId().toPostId())
        assertEquals(creatorId, creatorId.toFanboxCreatorId().toCreatorId())
        assertEquals(userId, userId.toFanboxUserId().toUserId())
        assertEquals(commentId, commentId.toFanboxCommentId().toCommentId())
    }

    @Test
    fun cursorRoundTripReturnsOriginalValue() {
        val original = FanboxCursor(
            firstPublishedDatetime = "2024-01-01T00:00:00Z",
            maxPublishedDatetime = "2024-01-02T00:00:00Z",
            firstId = "1",
            maxId = "2",
            limit = 10,
        )

        assertEquals(original, original.toCursor().toFanboxCursor())
    }

    /** 空リストや null カーソルでもページング型が破綻しないことを確認する。 */
    @Test
    fun pageCursorInfoHandlesEmptyContentsAndNullCursor() {
        val original = FanktPageCursorInfo<String>(contents = emptyList(), cursor = null)

        val converted = original.toPageCursorInfo { it }

        assertEquals(emptyList(), converted.contents)
        assertEquals(null, converted.cursor)
    }
}
