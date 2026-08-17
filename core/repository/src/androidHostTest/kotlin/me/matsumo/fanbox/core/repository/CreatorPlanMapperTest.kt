package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.repository.mapper.toCreatorPlan
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlan
import me.matsumo.fankt.fanbox.domain.model.FanboxPaymentMethod
import me.matsumo.fankt.fanbox.domain.model.FanboxUser
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPlanId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.junit.Test
import kotlin.test.assertEquals

/** [FanboxCreatorPlan] から [me.matsumo.fanbox.core.model.fanbox.CreatorPlan] への変換を検証するテスト。 */
class CreatorPlanMapperTest {

    private val fanboxCreatorPlan = FanboxCreatorPlan(
        id = FanboxPlanId("plan-1"),
        title = "title",
        description = "description",
        fee = 1000,
        coverImageUrl = "https://example.com/cover.png",
        hasAdultContent = true,
        paymentMethod = FanboxPaymentMethod.CARD,
        user = FanboxUser(
            userId = FanboxUserId(100),
            creatorId = FanboxCreatorId("creator-1"),
            name = "creator name",
            iconUrl = "https://example.com/icon.png",
        ),
    )

    /** 支援プランの全フィールドが値を保ったまま変換されることを確認する。 */
    @Test
    fun creatorPlanConversionPreservesAllFields() {
        val creatorPlan = fanboxCreatorPlan.toCreatorPlan()

        assertEquals(fanboxCreatorPlan.id.value, creatorPlan.id.value)
        assertEquals(fanboxCreatorPlan.title, creatorPlan.title)
        assertEquals(fanboxCreatorPlan.description, creatorPlan.description)
        assertEquals(fanboxCreatorPlan.fee, creatorPlan.fee)
        assertEquals(fanboxCreatorPlan.coverImageUrl, creatorPlan.coverImageUrl)
        assertEquals(fanboxCreatorPlan.hasAdultContent, creatorPlan.hasAdultContent)
        assertEquals(fanboxCreatorPlan.user?.userId?.value, creatorPlan.user?.userId?.value)
    }

    /**
     * fankt から移植した派生メンバーが、fankt の元実装と同じ値を返すことを確認する。
     * 独自ロジックで再実装すると計算式のズレに気付けないため、fankt 側の値と突き合わせる。
     */
    @Test
    fun derivedMembersMatchFanktValues() {
        val creatorPlan = fanboxCreatorPlan.toCreatorPlan()

        assertEquals(fanboxCreatorPlan.planBrowserUrl, creatorPlan.planBrowserUrl)
        assertEquals(fanboxCreatorPlan.supportingBrowserUrl, creatorPlan.supportingBrowserUrl)
    }
}
