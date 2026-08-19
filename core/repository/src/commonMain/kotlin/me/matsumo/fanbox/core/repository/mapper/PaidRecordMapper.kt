package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.PaidRecord
import me.matsumo.fankt.fanbox.domain.model.FanboxPaidRecord
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalTime::class)
fun FanboxPaidRecord.toPaidRecord(): PaidRecord {
    return PaidRecord(
        id = id,
        paidAmount = paidAmount,
        paymentDateTime = paymentDateTime,
        paymentMethod = paymentMethod.toPaymentMethod(),
        creator = creator.toCreator(),
    )
}
