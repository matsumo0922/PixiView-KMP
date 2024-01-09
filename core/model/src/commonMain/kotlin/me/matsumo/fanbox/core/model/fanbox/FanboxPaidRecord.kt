package me.matsumo.fanbox.core.model.fanbox

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

data class FanboxPaidRecord(
    val id: String,
    val paidAmount: Int,
    val paymentDateTime: Instant,
    val paymentMethod: PaymentMethod,
    val creator: FanboxCreator,
)
