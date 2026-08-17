package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.CreatorPlanDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlanDetail
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun FanboxCreatorPlanDetail.toCreatorPlanDetail(): CreatorPlanDetail {
    return CreatorPlanDetail(
        plan = plan.toCreatorPlan(),
        supportStartDatetime = supportStartDatetime,
        supportTransactions = supportTransactions.map { it.toSupportTransaction() },
        supporterCardImageUrl = supporterCardImageUrl,
    )
}

@OptIn(ExperimentalTime::class)
private fun FanboxCreatorPlanDetail.SupportTransaction.toSupportTransaction(): CreatorPlanDetail.SupportTransaction {
    return CreatorPlanDetail.SupportTransaction(
        id = id,
        paidAmount = paidAmount,
        transactionDatetime = transactionDatetime,
        targetMonth = targetMonth,
        user = user.toUser(),
    )
}
