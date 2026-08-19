package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.repository.mapper.toCreatorPlanDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlan
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlanDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxPaymentMethod
import me.matsumo.fankt.fanbox.domain.model.FanboxUser
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPlanId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * [FanboxCreatorPlanDetail] からアプリ所有のプラン詳細への変換を検証するテスト。
 *
 * 支援履歴を一覧で写すうえ、[FanboxCreatorPlanDetail.supportStartDatetime] と
 * [FanboxCreatorPlanDetail.SupportTransaction.targetMonth] がどちらも文字列である。
 */
@OptIn(ExperimentalTime::class)
class CreatorPlanDetailMapperTest {

    private val transactionDatetime = Instant.parse("2024-02-01T00:00:00Z")

    private val user = FanboxUser(
        userId = FanboxUserId(400),
        creatorId = FanboxCreatorId("creator-1"),
        name = "creator name",
        iconUrl = "https://example.com/icon.png",
    )

    private val fanboxPlanDetail = FanboxCreatorPlanDetail(
        plan = FanboxCreatorPlan(
            id = FanboxPlanId("plan-1"),
            title = "plan title",
            description = "plan description",
            fee = 1000,
            coverImageUrl = "https://example.com/plan.png",
            hasAdultContent = true,
            paymentMethod = FanboxPaymentMethod.CARD,
            user = user,
        ),
        supportStartDatetime = "2024-01-15",
        supportTransactions = listOf(
            FanboxCreatorPlanDetail.SupportTransaction(
                id = "transaction-1",
                paidAmount = 1000,
                transactionDatetime = transactionDatetime,
                targetMonth = "2024-02",
                user = user,
            ),
        ),
        supporterCardImageUrl = "https://example.com/card.png",
    )

    /** 入れ子のプランと支援履歴を含む全フィールドが写ることを確認する。 */
    @Test
    fun creatorPlanDetailConversionPreservesAllFields() {
        val planDetail = fanboxPlanDetail.toCreatorPlanDetail()

        assertEquals("plan-1", planDetail.plan.id.value)
        assertEquals("plan title", planDetail.plan.title)
        assertEquals(1000, planDetail.plan.fee)
        assertEquals("2024-01-15", planDetail.supportStartDatetime)
        assertEquals("https://example.com/card.png", planDetail.supporterCardImageUrl)

        val transaction = planDetail.supportTransactions.single()
        assertEquals("transaction-1", transaction.id)
        assertEquals(1000, transaction.paidAmount)
        assertEquals(transactionDatetime, transaction.transactionDatetime)
        assertEquals("2024-02", transaction.targetMonth)
        assertEquals(400L, transaction.user.userId?.value)
    }

    /** 支援履歴が無い場合も安全に変換できることを確認する。 */
    @Test
    fun creatorPlanDetailConversionHandlesEmptyTransactions() {
        val planDetail = fanboxPlanDetail.copy(supportTransactions = emptyList()).toCreatorPlanDetail()

        assertEquals(emptyList(), planDetail.supportTransactions)
    }
}
