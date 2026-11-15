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
    fun singlePostShowsTitleAndThumbnail() {
        val newPost = post("1", lastSeen, hasAdultContent = true)

        val result = resolveNewPostNotificationContent(listOf(newPost), isHideAdultContents = false)

        assertEquals(
            NewPostNotificationContent.Single(
                postId = PostId("1"),
                creatorId = CreatorId("creator-1"),
                creatorName = "Creator 1",
                title = "Title 1",
                thumbnailUrl = "https://example.com/1.png",
            ),
            result,
        )
    }

    @Test
    fun singleAdultPostHidesTitleAndThumbnailWhenHidingIsOn() {
        val newPost = post("1", lastSeen, hasAdultContent = true)

        val result = resolveNewPostNotificationContent(listOf(newPost), isHideAdultContents = true)

        assertEquals(
            NewPostNotificationContent.SingleHidden(
                postId = PostId("1"),
                creatorId = CreatorId("creator-1"),
                creatorName = "Creator 1",
            ),
            result,
        )
    }

    @Test
    fun singleAllAgesPostKeepsTitleWhenHidingIsOn() {
        val newPost = post("1", lastSeen, hasAdultContent = false)

        val result = resolveNewPostNotificationContent(listOf(newPost), isHideAdultContents = true)

        assertEquals("Title 1", (result as NewPostNotificationContent.Single).title)
    }

    @Test
    fun multiplePostsAreCombinedWithDistinctCreatorNames() {
        val newPosts = listOf(
            post("3", lastSeen, creatorIndex = 2),
            post("2", lastSeen, creatorIndex = 1, hasAdultContent = true),
            post("1", lastSeen, creatorIndex = 2),
        )

        val result = resolveNewPostNotificationContent(newPosts, isHideAdultContents = true)

        assertEquals(NewPostNotificationContent.Multiple(postCount = 3, creatorNames = listOf("Creator 2", "Creator 1")), result)
    }

    private fun post(
        id: String,
        publishedAt: Instant,
        creatorIndex: Int = id.toInt(),
        hasAdultContent: Boolean = false,
    ) = Post(
        id = PostId(id),
        title = "Title $id",
        cover = Cover(url = "https://example.com/$id.png", type = "cover"),
        user = User(
            userId = null,
            creatorId = CreatorId("creator-$creatorIndex"),
            name = "Creator $creatorIndex",
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
