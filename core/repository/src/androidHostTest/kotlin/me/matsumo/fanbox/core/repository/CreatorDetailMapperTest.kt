package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.model.fanbox.CreatorDetail
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail.Platform
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail.ProfileItem
import me.matsumo.fanbox.core.repository.mapper.toCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxUser
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail.Platform as FanktPlatform
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail.ProfileItem as FanktProfileItem
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail.ProfileLink as FanktProfileLink

/** [FanboxCreatorDetail] から [CreatorDetail] への変換を検証するテスト。 */
class CreatorDetailMapperTest {

    private val imageProfileItem = FanktProfileItem.Image(
        id = "profile-image",
        imageUrl = "https://example.com/profile.png",
        thumbnailUrl = "https://example.com/profile-thumb.png",
    )

    private val videoProfileItem = FanktProfileItem.Video(
        id = "profile-video",
        serviceProvider = "youtube",
        videoId = "video-id",
        thumbnailUrl = "https://example.com/video-thumb.png",
    )

    private val unknownProfileItem = FanktProfileItem.Unknown(
        id = "profile-unknown",
        type = "mystery",
        rawJson = """{"itemType":"mystery"}""",
    )

    private val fanboxCreatorDetail = FanboxCreatorDetail(
        creatorId = FanboxCreatorId("creator-1"),
        coverImageUrl = "https://example.com/cover.png",
        description = "description",
        hasAdultContent = true,
        hasBoothShop = true,
        isAcceptingRequest = true,
        isFollowed = false,
        isStopped = false,
        isSupported = true,
        profileItems = listOf(imageProfileItem, videoProfileItem, unknownProfileItem),
        profileLinks = listOf(
            FanktProfileLink(url = "https://twitter.com/example", link = FanktPlatform.TWITTER),
            FanktProfileLink(url = "https://example.com", link = FanktPlatform.UNKNOWN),
        ),
        user = FanboxUser(
            userId = FanboxUserId(100),
            creatorId = FanboxCreatorId("creator-1"),
            name = "creator name",
            iconUrl = "https://example.com/icon.png",
        ),
    )

    /** クリエイター詳細の全フィールドが値を保ったまま変換されることを確認する。値が欠けていても検出できないため。 */
    @Test
    fun creatorDetailConversionPreservesAllFields() {
        val creatorDetail = fanboxCreatorDetail.toCreatorDetail()

        assertEquals(fanboxCreatorDetail.creatorId.value, creatorDetail.creatorId.value)
        assertEquals(fanboxCreatorDetail.coverImageUrl, creatorDetail.coverImageUrl)
        assertEquals(fanboxCreatorDetail.description, creatorDetail.description)
        assertEquals(fanboxCreatorDetail.hasAdultContent, creatorDetail.hasAdultContent)
        assertEquals(fanboxCreatorDetail.hasBoothShop, creatorDetail.hasBoothShop)
        assertEquals(fanboxCreatorDetail.isAcceptingRequest, creatorDetail.isAcceptingRequest)
        assertEquals(fanboxCreatorDetail.isFollowed, creatorDetail.isFollowed)
        assertEquals(fanboxCreatorDetail.isStopped, creatorDetail.isStopped)
        assertEquals(fanboxCreatorDetail.isSupported, creatorDetail.isSupported)
        assertEquals(fanboxCreatorDetail.user?.userId?.value, creatorDetail.user?.userId?.value)
        assertEquals(fanboxCreatorDetail.user?.name, creatorDetail.user?.name)

        assertEquals(2, creatorDetail.profileLinks.size)
        assertEquals(fanboxCreatorDetail.profileLinks[0].url, creatorDetail.profileLinks[0].url)
        assertEquals(Platform.TWITTER, creatorDetail.profileLinks[0].link)
        assertEquals(Platform.UNKNOWN, creatorDetail.profileLinks[1].link)
    }

    /** プロフィールアイテムの3種類すべてが変換でき、未知種別は raw JSON を保持することを確認する。 */
    @Test
    fun profileItemVariantsConvertSuccessfullyAndPreserveRawJson() {
        val profileItems = fanboxCreatorDetail.toCreatorDetail().profileItems

        assertEquals(3, profileItems.size)

        val image = assertIs<ProfileItem.Image>(profileItems[0])
        assertEquals(imageProfileItem.id, image.id)
        assertEquals(imageProfileItem.imageUrl, image.imageUrl)

        val video = assertIs<ProfileItem.Video>(profileItems[1])
        assertEquals(videoProfileItem.id, video.id)
        assertEquals(videoProfileItem.serviceProvider, video.serviceProvider)

        val unknown = assertIs<ProfileItem.Unknown>(profileItems[2])
        assertEquals(unknownProfileItem.id, unknown.id)
        assertEquals(unknownProfileItem.type, unknown.type)
        assertEquals(unknownProfileItem.rawJson, unknown.rawJson)
    }

    /**
     * fankt から移植した派生メンバーが、fankt の元実装と同じ値を返すことを確認する。
     * 独自ロジックで再実装すると計算式のズレに気付けないため、fankt 側の値と突き合わせる。
     */
    @Test
    fun derivedMembersMatchFanktValues() {
        val creatorDetail = fanboxCreatorDetail.toCreatorDetail()

        assertEquals(fanboxCreatorDetail.supportingBrowserUrl, creatorDetail.supportingBrowserUrl)

        val video = assertIs<ProfileItem.Video>(creatorDetail.profileItems[1])
        assertEquals(videoProfileItem.url, video.url)
    }
}
