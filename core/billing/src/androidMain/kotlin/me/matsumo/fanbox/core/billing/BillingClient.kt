package me.matsumo.fanbox.core.billing

import android.app.Activity
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import me.matsumo.fanbox.core.billing.models.FeatureType
import me.matsumo.fanbox.core.billing.models.ProductDetails
import me.matsumo.fanbox.core.billing.models.ProductId
import me.matsumo.fanbox.core.billing.models.ProductType
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

interface BillingClient {
    fun initialize()
    fun dispose()

    suspend fun verifyFeatureSupported(featureType: FeatureType): Boolean
    suspend fun verifyFeaturesSupported(featureTypes: List<FeatureType>): Map<FeatureType, Boolean>
    suspend fun queryProductDetails(productId: ProductId, productType: ProductType): ProductDetails
    suspend fun queryProductDetailsList(productDetailsCommand: QueryProductDetailsCommand, productType: ProductType): List<ProductDetails>
    suspend fun queryPurchases(productType: ProductType): List<Purchase>
    suspend fun queryPurchaseHistory(productType: ProductType): List<PurchaseHistoryRecord>
    suspend fun consumePurchase(purchase: Purchase): ConsumeResult
    suspend fun acknowledgePurchase(purchase: Purchase): AcknowledgeResult
    suspend fun launchBillingFlow(activity: Activity, command: PurchaseSingleCommand): SingleBillingFlowResult
}

class BillingClientImpl(
    private val provider: BillingClientProvider,
) : BillingClient {

    override fun initialize() {
        provider.initialize()
    }

    override fun dispose() {
        provider.dispose()
    }

    override suspend fun verifyFeatureSupported(featureType: FeatureType) = suspendCancellableCoroutine {
        provider.verifyFeatureSupported(featureType, contListener(it))
    }

    override suspend fun verifyFeaturesSupported(featureTypes: List<FeatureType>) = suspendCancellableCoroutine {
        provider.verifyFeaturesSupported(featureTypes, contListener(it))
    }

    override suspend fun queryProductDetailsList(productDetailsCommand: QueryProductDetailsCommand, productType: ProductType) = suspendCancellableCoroutine {
        provider.queryProductDetailsList(productDetailsCommand, productType, contListener(it))
    }

    override suspend fun queryPurchases(productType: ProductType) = suspendCancellableCoroutine {
        provider.queryPurchases(productType, contListener(it))
    }

    override suspend fun queryProductDetails(productId: ProductId, productType: ProductType) = suspendCancellableCoroutine {
        provider.queryProductDetails(productId, productType, contListener(it))
    }

    override suspend fun queryPurchaseHistory(productType: ProductType) = suspendCancellableCoroutine {
        provider.queryPurchaseHistory(productType, contListener(it))
    }

    override suspend fun consumePurchase(purchase: Purchase) = suspendCancellableCoroutine {
        provider.consumePurchase(purchase, contListener(it))
    }

    override suspend fun acknowledgePurchase(purchase: Purchase) = suspendCancellableCoroutine {
        provider.acknowledgePurchase(purchase, contListener(it))
    }

    override suspend fun launchBillingFlow(activity: Activity, command: PurchaseSingleCommand) = suspendCancellableCoroutine {
        provider.launchBillingFlow(activity, command, contListener(it))
    }

    private fun <T> contListener(cont: CancellableContinuation<T>): (Result<T>) -> Unit {
        return { result ->
            result.fold(
                onSuccess = {
                    if (!cont.isCancelled) {
                        cont.resume(it)
                    }
                },
                onFailure = {
                    if (!cont.isCancelled) {
                        cont.resumeWithException(it)
                    }
                },
            )
        }
    }
}
