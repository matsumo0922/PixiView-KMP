package me.matsumo.fanbox.core.model.fanbox

enum class PaymentMethod {
    CARD,
    PAYPAL,
    CVS,
    UNKNOWN,
    ;

    companion object {
        fun fromString(string: String?) = when (string) {
            "gmo_card" -> CARD
            "paypal" -> PAYPAL
            "gmo_cvs" -> CVS
            else -> UNKNOWN
        }
    }
}
