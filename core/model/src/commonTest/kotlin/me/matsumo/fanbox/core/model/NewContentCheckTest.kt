package me.matsumo.fanbox.core.model

import me.matsumo.fanbox.core.model.fanbox.Cover
import me.matsumo.fanbox.core.model.fanbox.Creator
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.NewsLetter
import me.matsumo.fanbox.core.model.fanbox.NewsLetterId
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.User
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 新着の定期確認の、通信の前の判定、遠隔の設定の解釈、新着投稿・新着メッセージの判定、新着投稿の通知に表示する内容の決定を検証するテスト。 */
@OptIn(ExperimentalTime::class)
class NewContentCheckTest {

    private val lastSeen = Instant.parse("2026-11-01T12:00:00Z")

    @Test
    fun preconditionProceedsOnlyWhenEveryConditionHolds() {
        val result = resolveNewContentCheckPrecondition(
            isNotificationEnabled = true,
            isTestUser = false,
            canNotify = true,
            hasStoredSession = true,
        )

        assertEquals(NewContentCheckPrecondition.PROCEED, result)
    }

    @Test
    fun preconditionSkipsWhenTheSettingIsOffOrForTestUsers() {
        val disabled = resolveNewContentCheckPrecondition(
            isNotificationEnabled = false,
            isTestUser = false,
            canNotify = false,
            hasStoredSession = true,
        )
        val testUser = resolveNewContentCheckPrecondition(
            isNotificationEnabled = true,
            isTestUser = true,
            canNotify = false,
            hasStoredSession = true,
        )

        assertEquals(NewContentCheckPrecondition.SKIP, disabled)
        assertEquals(NewContentCheckPrecondition.SKIP, testUser)
    }

    @Test
    fun preconditionClearsTheRecordWhenNotificationsCannotBeShown() {
        val result = resolveNewContentCheckPrecondition(
            isNotificationEnabled = true,
            isTestUser = false,
            canNotify = false,
            hasStoredSession = false,
        )

        assertEquals(NewContentCheckPrecondition.CLEAR_RECORD_AND_SKIP, result)
    }

    @Test
    fun preconditionSkipsWithoutAStoredSession() {
        val result = resolveNewContentCheckPrecondition(
            isNotificationEnabled = true,
            isTestUser = false,
            canNotify = true,
            hasStoredSession = false,
        )

        assertEquals(NewContentCheckPrecondition.SKIP, result)
    }

    @Test
    fun remoteConfigReadsValidValues() {
        val result = resolveNewContentCheckConfig(
            mapOf(
                "newPostCheck.enabled" to "false",
                "newMessageCheck.enabled" to "true",
                "newContentCheck.intervalHours" to "6",
            ),
        )

        assertEquals(
            NewContentCheckConfig(
                isPostCheckEnabled = false,
                isMessageCheckEnabled = true,
                intervalHours = 6,
            ),
            result,
        )
    }

    @Test
    fun remoteConfigFallsBackToDefaultWithoutMetadata() {
        assertEquals(NewContentCheckConfig.Default, resolveNewContentCheckConfig(emptyMap()))
    }

    @Test
    fun remoteConfigFallsBackToDefaultForMalformedValues() {
        val malformedValues = listOf("yes", "TRUE", "1", "")

        for (value in malformedValues) {
            val result = resolveNewContentCheckConfig(
                mapOf(
                    "newPostCheck.enabled" to value,
                    "newMessageCheck.enabled" to value,
                ),
            )

            assertEquals(NewContentCheckConfig.Default.isPostCheckEnabled, result.isPostCheckEnabled, value)
            assertEquals(NewContentCheckConfig.Default.isMessageCheckEnabled, result.isMessageCheckEnabled, value)
        }
    }

    @Test
    fun remoteConfigFallsBackToDefaultIntervalOutOfRange() {
        val outOfRangeValues = listOf("0", "-3", "169", "1.5", "three")

        for (value in outOfRangeValues) {
            val result = resolveNewContentCheckConfig(mapOf("newContentCheck.intervalHours" to value))

            assertEquals(NewContentCheckConfig.Default.intervalHours, result.intervalHours, value)
        }
    }

    @Test
    fun remoteConfigAcceptsIntervalAtTheEdgesOfTheRange() {
        assertEquals(1, resolveNewContentCheckConfig(mapOf("newContentCheck.intervalHours" to "1")).intervalHours)
        assertEquals(168, resolveNewContentCheckConfig(mapOf("newContentCheck.intervalHours" to "168")).intervalHours)
    }

    @Test
    fun firstCheckRecordsTheLatestPostWithoutNotifying() {
        val older = post("1", lastSeen)
        val newer = post("2", lastSeen + 1.hours)

        val result = detectNewPosts(listOf(older, newer), lastSeenPublishedAt = null)

        assertEquals(NewContentDetection.FirstCheck(newer.publishedDatetime), result)
    }

    @Test
    fun onlyPostsPublishedAfterTheRecordAreNew() {
        val recorded = post("1", lastSeen)
        val older = post("0", lastSeen - 1.seconds)
        val justAfter = post("2", lastSeen + 1.seconds)
        val latest = post("3", lastSeen + 2.hours)

        val result = detectNewPosts(listOf(justAfter, recorded, latest, older), lastSeen)

        assertEquals(NewContentDetection.Found(listOf(latest, justAfter), latest.publishedDatetime), result)
    }

    @Test
    fun thePostAlreadyRecordedIsNotNewAgain() {
        val recorded = post("1", lastSeen)
        val older = post("0", lastSeen - 1.hours)

        assertEquals(NewContentDetection.NotFound, detectNewPosts(listOf(recorded, older), lastSeen))
    }

    @Test
    fun noPostsLeaveTheRecordUnchanged() {
        assertEquals(NewContentDetection.NotFound, detectNewPosts(emptyList(), lastSeenPublishedAt = null))
        assertEquals(NewContentDetection.NotFound, detectNewPosts(emptyList(), lastSeen))
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

    @Test
    fun remoteConfigDisablesOnlyTheMessageCheck() {
        val result = resolveNewContentCheckConfig(mapOf("newMessageCheck.enabled" to "false"))

        assertEquals(NewContentCheckConfig.Default.copy(isMessageCheckEnabled = false), result)
    }

    @Test
    fun remoteConfigIgnoresTheRetiredIntervalKey() {
        val result = resolveNewContentCheckConfig(mapOf("newPostCheck.intervalHours" to "6"))

        assertEquals(NewContentCheckConfig.Default.intervalHours, result.intervalHours)
    }

    @Test
    fun firstMessageCheckRecordsTheLatestMessageWithoutNotifying() {
        val older = message("1", lastSeen)
        val newer = message("2", lastSeen + 1.hours, isRead = true)

        val result = detectNewMessages(listOf(older, newer), lastSeenCreatedAt = null, blockedCreatorIds = emptySet())

        assertEquals(NewContentDetection.FirstCheck(newer.createdAt), result)
    }

    @Test
    fun onlyUnreadMessagesCreatedAfterTheRecordAreNew() {
        val recorded = message("1", lastSeen)
        val older = message("0", lastSeen - 1.seconds)
        val justAfter = message("2", lastSeen + 1.seconds)
        val latest = message("3", lastSeen + 2.hours)

        val result = detectNewMessages(listOf(justAfter, recorded, latest, older), lastSeen, blockedCreatorIds = emptySet())

        assertEquals(NewContentDetection.Found(listOf(latest, justAfter), latest.createdAt), result)
    }

    @Test
    fun readMessagesAndMessagesFromBlockedCreatorsAreNotNew() {
        val read = message("1", lastSeen + 1.hours, isRead = true)
        val blocked = message("2", lastSeen + 2.hours)
        val unread = message("3", lastSeen + 3.hours)
        val latestRead = message("4", lastSeen + 4.hours, isRead = true)

        val result = detectNewMessages(
            messages = listOf(read, blocked, unread, latestRead),
            lastSeenCreatedAt = lastSeen,
            blockedCreatorIds = setOf(CreatorId("creator-2")),
        )

        assertEquals(NewContentDetection.Found(listOf(unread), latestRead.createdAt), result)
    }

    @Test
    fun noMessagesToNotifyLeaveTheRecordUnchanged() {
        val read = message("1", lastSeen + 1.hours, isRead = true)
        val recorded = message("0", lastSeen)

        assertEquals(NewContentDetection.NotFound, detectNewMessages(listOf(read, recorded), lastSeen, blockedCreatorIds = emptySet()))
        assertEquals(NewContentDetection.NotFound, detectNewMessages(emptyList(), lastSeenCreatedAt = null, blockedCreatorIds = emptySet()))
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

    private fun message(
        id: String,
        createdAt: Instant,
        isRead: Boolean = false,
    ) = NewsLetter(
        id = NewsLetterId(id),
        body = "Body $id",
        createdAt = createdAt,
        creator = Creator(
            creatorId = CreatorId("creator-$id"),
            user = null,
        ),
        isRead = isRead,
    )
}
