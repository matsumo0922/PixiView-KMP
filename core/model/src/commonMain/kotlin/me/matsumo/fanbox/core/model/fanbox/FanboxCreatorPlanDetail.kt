package me.matsumo.fanbox.core.model.fanbox

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

data class FanboxCreatorPlanDetail(
    val plan: FanboxCreatorPlan,
    val supportStartDatetime: String,
    val supportTransactions: List<SupportTransaction>,
    val supporterCardImageUrl: String,
) {
    data class SupportTransaction(
        val id: String,
        val paidAmount: Int,
        val transactionDatetime: Instant,
        val targetMonth: String,
        val user: FanboxUser,
    )

    companion object {
        fun dummy() = FanboxCreatorPlanDetail(
            plan = FanboxCreatorPlan.dummy(),
            supportStartDatetime = "",
            supportTransactions = listOf(
                SupportTransaction(
                    id = "",
                    paidAmount = 0,
                    transactionDatetime = Instant.DISTANT_PAST,
                    targetMonth = "",
                    user = FanboxUser.dummy(),
                ),
            ),
            supporterCardImageUrl = "",
        )
    }
}
