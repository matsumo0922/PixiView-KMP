package me.matsumo.fanbox.feature.about.billing

import me.matsumo.fanbox.core.model.BillingPlan

sealed interface BillingViewEvent {
    data class OnPlanSelected(val type: BillingPlan.Type) : BillingViewEvent
    data object OnPurchaseClicked : BillingViewEvent
    data object OnRestoreClicked : BillingViewEvent
}
