package me.matsumo.fanbox.feature.about.billing

import androidx.compose.runtime.Stable
import org.jetbrains.compose.resources.StringResource

@Stable
sealed interface BillingMessageEvent {
    data object Purchased : BillingMessageEvent
    data class SnackBar(val messageRes: StringResource) : BillingMessageEvent
}
