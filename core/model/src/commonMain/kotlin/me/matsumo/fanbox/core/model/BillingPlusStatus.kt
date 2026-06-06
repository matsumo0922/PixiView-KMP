package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable

/** Plus の利用状態を表すモデル。 */
@Immutable
data class BillingPlusStatus(
    val isActive: Boolean,
    val isTrial: Boolean,
)
