package me.matsumo.fanbox.core.model

data class BillingPlan(
    val id: String,
    val price: Long,
    val formattedPrice: String,
    val type: Type,
) {
    enum class Type {
        MONTHLY,
        ANNUAL,
        UNKNOWN
    }
}
