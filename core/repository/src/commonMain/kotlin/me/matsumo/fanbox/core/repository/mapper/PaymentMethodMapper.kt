package me.matsumo.fanbox.core.repository.mapper

import me.matsumo.fanbox.core.model.fanbox.PaymentMethod
import me.matsumo.fankt.fanbox.domain.model.FanboxPaymentMethod

fun FanboxPaymentMethod.toPaymentMethod(): PaymentMethod {
    return when (this) {
        FanboxPaymentMethod.CARD -> PaymentMethod.CARD
        FanboxPaymentMethod.PAYPAL -> PaymentMethod.PAYPAL
        FanboxPaymentMethod.CVS -> PaymentMethod.CVS
        FanboxPaymentMethod.UNKNOWN -> PaymentMethod.UNKNOWN
    }
}
