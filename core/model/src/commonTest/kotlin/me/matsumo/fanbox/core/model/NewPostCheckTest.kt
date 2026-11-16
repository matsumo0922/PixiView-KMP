package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.model.fanbox.Cover
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 新着投稿の定期確認の、遠隔の設定の解釈、新着の判定、通知に表示する内容の決定を検証するテスト。 */
@OptIn(ExperimentalTime::class)
class NewPostCheckTest {

    private val lastSeen = Instant.parse("2026-11-01T12:00:00Z")

    @Test
    fun preconditionProceedsOnlyWhenEveryConditionHolds() {
        val result = resolveNewPostCheckPrecondition(
            isNotificationEnabled = true,
            isTestUser = false,
            canNotify = true,
            hasStoredSession = true,
        )

        assertEquals(NewPostCheckPrecondition.PROCEED, result)
    }

    @Test
    fun preconditionSkipsWhenTheSettingIsOffOrForTestUsers() {
        val disabled = resolveNewPostCheckPrecondition(
            isNotificationEnabled = false,
            isTestUser = false,
            canNotify = false,
            hasStoredSession = true,
        )
        val testUser = resolveNewPostCheckPrecondition(
            isNotificationEnabled = true,
            isTestUser = true,
            canNotify = false,
            hasStoredSession = true,
        )

        assertEquals(NewPostCheckPrecondition.SKIP, disabled)
        assertEquals(NewPostCheckPrecondition.SKIP, testUser)
    }

    @Test
    fun preconditionClearsTheRecordWhenNotificationsCannotBeShown() {
        val result = resolveNewPostCheckPrecondition(
            isNotificationEnabled = true,
            isTestUser = false,
            canNotify = false,
            hasStoredSession = false,
        )

        assertEquals(NewPostCheckPrecondition.CLEAR_RECORD_AND_SKIP, result)
    }

    @Test
    fun preconditionSkipsWithoutAStoredSession() {
        val result = resolveNewPostCheckPrecondition(
            isNotificationEnabled = true,
            isTestUser = false,
            canNotify = true,
            hasStoredSession = false,
        )

        assertEquals(NewPostCheckPrecondition.SKIP, result)
    }

    @Test
    fun remoteConfigReadsValidValues() {
        val result = resolveNewPostCheckConfig(
            mapOf(
                "newPostCheck.enabled" to "false",
                "newPostCheck.intervalHours" to "6",
            ),
        )

        assertEquals(NewPostCheckConfig(isEnabled = false, intervalHours = 6), result)
    }

    @Test
    fun remoteConfigFallsBackToDefaultWithoutMetadata() {
        assertEquals(NewPostCheckConfig.Default, resolveNewPostCheckConfig(emptyMap()))
    }

    @Test
    fun remoteConfigFallsBackToDefaultForMalformedValues() {
        val malformedValues = listOf("yes", "TRUE", "1", "")

        for (value in malformedValues) {
            val result = resolveNewPostCheckConfig(mapOf("newPostCheck.enabled" to value))

            assertEquals(NewPostCheckConfig.Default.isEnabled, result.isEnabled, value)
        }
    }

    @Test
    fun remoteConfigFallsBackToDefaultIntervalOutOfRange() {
        val outOfRangeValues = listOf("0", "-3", "169", "1.5", "three")

        for (value in outOfRangeValues) {
            val result = resolveNewPostCheckConfig(mapOf("newPostCheck.intervalHours" to value))

            assertEquals(NewPostCheckConfig.Default.intervalHours, result.intervalHours, value)
        }
    }

    @Test
    fun remoteConfigAcceptsIntervalAtTheEdgesOfTheRange() {
        assertEquals(1, resolveNewPostCheckConfig(mapOf("newPostCheck.intervalHours" to "1")).intervalHours)
        assertEquals(168, resolveNewPostCheckConfig(mapOf("newPostCheck.intervalHours" to "168")).intervalHours)
    }

    @Test
    fun firstCheckRecordsTheLatestPostWithoutNotifying() {
        val older = post("1", lastSeen)
        val newer = post("2", lastSeen + 1.hours)

        val result = detectNewPosts(listOf(older, newer), lastSeenPublishedAt = null)

        assertEquals(NewPostDetection.FirstCheck(newer.publishedDatetime), result)
    }

    @Test
    fun onlyPostsPublishedAfterTheRecordAreNew() {
        val recorded = post("1", lastSeen)
        val older = post("0", lastSeen - 1.seconds)
        val justAfter = post("2", lastSeen + 1.seconds)
        val latest = post("3", lastSeen + 2.hours)

        val result = detectNewPosts(listOf(justAfter, recorded, latest, older), lastSeen)

        assertEquals(NewPostDetection.Found(listOf(latest, justAfter), latest.publishedDatetime), result)
    }

    @Test
    fun thePostAlreadyRecordedIsNotNewAgain() {
        val recorded = post("1", lastSeen)
        val older = post("0", lastSeen - 1.hours)

        assertEquals(NewPostDetection.NotFound, detectNewPosts(listOf(recorded, older), lastSeen))
    }

    @Test
    fun noPostsLeaveTheRecordUnchanged() {
        assertEquals(NewPostDetection.NotFound, detectNewPosts(emptyList(), lastSeenPublishedAt = null))
        assertEquals(NewPostDetection.NotFound, detectNewPosts(emptyList(), lastSeen))
    }

    @Test
    fun postShowsTitleAndThumbnail() {
        val newPost = post("1", lastSeen, hasAdultContent = true)

        val result = resolveNewPostNotificationContent(newPost, isHideAdultContents = false)

        assertEquals(
            NewPostNotificationContent.Visible(
                postId = PostId("1"),
                creatorId = CreatorId("creator-1"),
                creatorName = "Creator 1",
                publishedAt = lastSeen,
                title = "Title 1",
                thumbnailUrl = "https://example.com/1.png",
            ),
            result,
        )
    }

    @Test
    fun adultPostHidesTitleAndThumbnailWhenHidingIsOn() {
        val newPost = post("1", lastSeen, hasAdultContent = true)

        val result = resolveNewPostNotificationContent(newPost, isHideAdultContents = true)

        assertEquals(
            NewPostNotificationContent.Hidden(
                postId = PostId("1"),
                creatorId = CreatorId("creator-1"),
                creatorName = "Creator 1",
                publishedAt = lastSeen,
            ),
            result,
        )
    }

    @Test
    fun allAgesPostKeepsTitleWhenHidingIsOn() {
        val newPost = post("1", lastSeen, hasAdultContent = false)

        val result = resolveNewPostNotificationContent(newPost, isHideAdultContents = true)

        assertEquals("Title 1", (result as NewPostNotificationContent.Visible).title)
    }

    private fun post(
        id: String,
        publishedAt: Instant,
        hasAdultContent: Boolean = false,
    ) = Post(
        id = PostId(id),
        title = "Title $id",
        cover = Cover(url = "https://example.com/$id.png", type = "cover"),
        user = User(
            userId = null,
            creatorId = CreatorId("creator-$id"),
            name = "Creator $id",
            iconUrl = null,
        ),
        excerpt = "",
        feeRequired = 0,
        hasAdultContent = hasAdultContent,
        isLiked = false,
        isRestricted = false,
        likeCount = 0,
        commentCount = 0,
        tags = emptyList(),
        publishedDatetime = publishedAt,
        updatedDatetime = publishedAt,
    )
}
