package me.matsumo.fanbox.core.billing.usecase

import com.android.billingclient.api.Purchase
import me.matsumo.fanbox.core.billing.PurchaseSingleCommand
import me.matsumo.fanbox.core.billing.models.ProductDetails

data class PurchaseConsumableResult(
    val command: PurchaseSingleCommand,
    val productDetails: ProductDetails,
    val purchase: Purchase,
)
