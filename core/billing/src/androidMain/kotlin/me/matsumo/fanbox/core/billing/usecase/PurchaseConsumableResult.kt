package me.matsumo.fanbox.core.billing.usecase

import me.matsumo.fanbox.core.billing.PurchaseSingleCommand
import me.matsumo.fanbox.core.billing.models.ProductDetails
import com.android.billingclient.api.Purchase

data class PurchaseConsumableResult(
    val command: PurchaseSingleCommand,
    val productDetails: ProductDetails,
    val purchase: Purchase,
)
