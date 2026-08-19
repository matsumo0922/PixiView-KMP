package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/** 支援の決済記録。 */
@Immutable
@OptIn(ExperimentalTime::class)
data class PaidRecord(
    val id: String,
    val paidAmount: Int,
    val paymentDateTime: Instant,
    val paymentMethod: PaymentMethod,
    val creator: Creator,
)
