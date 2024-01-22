package me.matsumo.fanbox.core.billing

import io.github.aakira.napier.Napier
import kotlinx.coroutines.suspendCancellableCoroutine
import platform.StoreKit.SKProduct
import platform.StoreKit.SKProductsRequest
import platform.StoreKit.SKProductsRequestDelegateProtocol
import platform.StoreKit.SKProductsResponse
import platform.darwin.NSObject
import kotlin.coroutines.resume

interface BillingClient {
    suspend fun queryProductDetails(productIds: Set<String>): List<SKProduct>
}

class BillingClientImpl : BillingClient {

    override suspend fun queryProductDetails(productIds: Set<String>): List<SKProduct> = suspendCancellableCoroutine {
        val delegate = object : NSObject(), SKProductsRequestDelegateProtocol {
            override fun productsRequest(
                request: SKProductsRequest,
                didReceiveResponse: SKProductsResponse
            ) {
                if (didReceiveResponse.invalidProductIdentifiers.isNotEmpty()) {
                    Napier.e("invalid product identifiers: ${didReceiveResponse.invalidProductIdentifiers}")
                    it.resume(emptyList())
                    return
                }

                if (didReceiveResponse.products.isEmpty()) {
                    Napier.e("products is empty")
                    it.resume(emptyList())
                    return
                }

                it.resume(didReceiveResponse.products.filterIsInstance<SKProduct>())
            }
        }

        SKProductsRequest(productIds).also { request ->
            request.setDelegate(delegate)
            request.start()
        }
    }
}
