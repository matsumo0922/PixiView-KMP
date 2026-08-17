package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 支援プランの詳細と支援履歴。 */
@Immutable
data class CreatorPlanDetail(
    val plan: CreatorPlan,
    val supportStartDatetime: String,
    val supportTransactions: List<SupportTransaction>,
    val supporterCardImageUrl: String,
) {
    /** 支援トランザクション。 */
    @Immutable
    @OptIn(ExperimentalTime::class)
    data class SupportTransaction(
        val id: String,
        val paidAmount: Int,
        val transactionDatetime: Instant,
        val targetMonth: String,
        val user: User,
    )
}
