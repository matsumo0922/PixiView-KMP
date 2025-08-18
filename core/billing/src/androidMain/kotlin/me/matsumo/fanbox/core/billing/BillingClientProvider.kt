package me.matsumo.fanbox.core.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClient.BillingResponseCode
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchaseHistoryRecord
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryPurchaseHistoryParams
import com.android.billingclient.api.QueryPurchasesParams
import io.github.aakira.napier.Napier
import me.matsumo.fanbox.core.billing.models.FeatureType
import me.matsumo.fanbox.core.billing.models.ProductDetails
import me.matsumo.fanbox.core.billing.models.ProductId
import me.matsumo.fanbox.core.billing.models.ProductType
import me.matsumo.fanbox.core.billing.models.translate

private typealias ResponseListener<T> = (Result<T>) -> Unit

interface BillingClientProvider {

    fun initialize()
    fun dispose()

    fun verifyFeatureSupported(featureType: FeatureType, listener: ResponseListener<Boolean>)
    fun verifyFeaturesSupported(featureTypes: List<FeatureType>, listener: ResponseListener<Map<FeatureType, Boolean>>)
    fun queryProductDetails(productId: ProductId, productType: ProductType, listener: ResponseListener<ProductDetails>)
    fun queryProductDetailsList(productDetailsCommand: QueryProductDetailsCommand, productType: ProductType, listener: ResponseListener<List<ProductDetails>>)
    fun queryPurchases(productType: ProductType, listener: ResponseListener<List<Purchase>>)
    fun queryPurchaseHistory(productType: ProductType, listener: ResponseListener<List<PurchaseHistoryRecord>>)
    fun consumePurchase(purchase: Purchase, listener: ResponseListener<ConsumeResult>)
    fun acknowledgePurchase(purchase: Purchase, listener: ResponseListener<AcknowledgeResult>)
    fun launchBillingFlow(activity: Activity, command: PurchaseSingleCommand, listener: ResponseListener<SingleBillingFlowResult>)

    enum class State {
        CONNECTING,
        CONNECTED,
        DISCONNECTED,
        DISPOSED,
        UNAVAILABLE,
    }
}

class BillingClientProviderImpl(context: Context) : BillingClientProvider {

    private val initializeResponseListeners = mutableListOf<ResponseListener<Unit>>()
    private val compositeListener = CompositePurchasesUpdatedListener()

    private val billingClient = BillingClient
        .newBuilder(context)
        .setListener(compositeListener)
        .enablePendingPurchases(
            PendingPurchasesParams.newBuilder()
                .enableOneTimeProducts()  // 一回限りのアイテムをサポート
                .enablePrepaidPlans()     // 任意: 定期購入の保留をサポートする場合
                .build()
        )
        .build()

    private var state = BillingClientProvider.State.DISCONNECTED

    override fun initialize() {
        if (state == BillingClientProvider.State.DISPOSED) {
            Napier.d("BillingClient already disposed")
            return
        }

        if (state == BillingClientProvider.State.UNAVAILABLE) {
            Napier.d("BillingClient unavailable")
            return
        }

        if (state == BillingClientProvider.State.CONNECTED) {
            Napier.d("BillingClient already connected")
            return
        }

        if (state == BillingClientProvider.State.CONNECTING) {
            Napier.d("BillingClient is connecting")
            return
        }

        state = BillingClientProvider.State.CONNECTING

        billingClient.startConnection(
            object : BillingClientStateListener {
                override fun onBillingSetupFinished(billingResult: BillingResult) {
                    state = when (val response = billingResult.toResponse()) {
                        is BillingResponse.OK -> {
                            Napier.d("BillingClient connected")
                            initializeResponseListeners.forEach { it.invoke(Result.success(Unit)) }
                            BillingClientProvider.State.CONNECTED
                        }
                        is BillingResponse.BillingUnavailable -> {
                            Napier.d("BillingClient unavailable")
                            initializeResponseListeners.forEach { it.invoke(Result.failure(InitializationFailedException(response))) }
                            BillingClientProvider.State.UNAVAILABLE
                        }
                        else -> {
                            Napier.d("BillingClient connection failed: $response")
                            initializeResponseListeners.forEach { it.invoke(Result.failure(InitializationFailedException(response))) }
                            BillingClientProvider.State.DISPOSED
                        }
                    }
                }

                override fun onBillingServiceDisconnected() {
                    Napier.d("BillingClient disconnected")
                    state = BillingClientProvider.State.DISCONNECTED
                }
            },
        )
    }

    override fun dispose() {
        billingClient.endConnection()
        state = BillingClientProvider.State.DISPOSED
    }

    override fun verifyFeatureSupported(
        featureType: FeatureType,
        listener: ResponseListener<Boolean>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        verifyFeaturesSupported(listOf(featureType)) { result ->
            result.fold(
                onSuccess = { listener.invoke(Result.success(it[featureType] ?: false)) },
                onFailure = {
                    if (it is VerifyFeaturesSupportedFailedException) {
                        listener.invoke(
                            Result.failure(
                                VerifyFeatureSupportedFailedException(
                                    response = billingResponse(BillingResponseCode.FEATURE_NOT_SUPPORTED),
                                    feature = featureType,
                                    isCalledAfterDispose = it.isCalledAfterDispose,
                                    isFailedOnInitialize = it.isFailedOnInitialize,
                                ),
                            ),
                        )
                    } else {
                        listener.invoke(Result.failure(it))
                    }
                },
            )
        }
    }

    override fun verifyFeaturesSupported(
        featureTypes: List<FeatureType>,
        listener: ResponseListener<Map<FeatureType, Boolean>>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        val resultSet = mutableMapOf<FeatureType, Boolean>()

        for (featureType in featureTypes) {
            when (val response = billingClient.isFeatureSupported(featureType.rawValue).toResponse()) {
                is BillingResponse.OK -> resultSet[featureType] = true
                is BillingResponse.FeatureNotSupported -> resultSet[featureType] = false
                is BillingResponse.ServiceDisconnected, is BillingResponse.ServiceError -> {
                    Napier.d("verifyFeatureSupported: service error. CODE=${response.code}")
                    state = BillingClientProvider.State.DISPOSED
                    listener.invoke(Result.failure(VerifyFeatureSupportedFailedException(response, featureType)))
                    return
                }
                else -> {
                    listener.invoke(Result.failure(VerifyFeatureSupportedFailedException(response, featureType)))
                    return
                }
            }
        }

        if (resultSet.values.all { it }) {
            listener.invoke(Result.success(resultSet))
        } else {
            listener.invoke(
                Result.failure(
                    VerifyFeaturesSupportedFailedException(
                        billingResponse(BillingResponseCode.FEATURE_NOT_SUPPORTED),
                        FeaturesSupportedResult(resultSet),
                    ),
                ),
            )
        }
    }

    override fun queryProductDetails(
        productId: ProductId,
        productType: ProductType,
        listener: ResponseListener<ProductDetails>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        queryProductDetailsList(QueryProductDetailsCommand(listOf(productId)), productType) { result ->
            result.fold(
                onSuccess = {
                    if (it.isEmpty()) {
                        listener.invoke(Result.failure(QueryProductDetailsFailedException(billingResponse(BillingResponseCode.ITEM_UNAVAILABLE), productId)))
                    } else {
                        listener.invoke(Result.success(it.first()))
                    }
                },
                onFailure = { listener.invoke(Result.failure(it)) },
            )
        }
    }

    override fun queryProductDetailsList(
        productDetailsCommand: QueryProductDetailsCommand,
        productType: ProductType,
        listener: ResponseListener<List<ProductDetails>>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        /*        billingClient.queryProductDetailsAsync(productDetailsCommand.toQueryProductDetailsParams(productType)) { result, products ->
                    when (val response = result.toResponse()) {
                        is BillingResponse.OK -> {
                            listener.invoke(Result.success(products.translate()))
                        }
                        is BillingResponse.ServiceDisconnected, is BillingResponse.ServiceError -> {
                            Napier.d("queryProductDetails: service error. CODE=${response.code}")
                            state = BillingClientProvider.State.DISPOSED
                            listener.invoke(Result.failure(QueryProductDetailsListFailedException(response, productDetailsCommand)))
                        }
                        else -> {
                            listener.invoke(Result.failure(QueryProductDetailsListFailedException(response, productDetailsCommand)))
                        }
                    }
                }*/
    }

    override fun queryPurchases(
        productType: ProductType,
        listener: ResponseListener<List<Purchase>>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(productType.rawValue)
            .build()

        billingClient.queryPurchasesAsync(params) { result, purchases ->
            when (val response = result.toResponse()) {
                is BillingResponse.OK -> {
                    listener.invoke(Result.success(purchases))
                }
                is BillingResponse.ServiceDisconnected, is BillingResponse.ServiceError -> {
                    Napier.d("queryPurchases: service error. CODE=${response.code}")
                    state = BillingClientProvider.State.DISPOSED
                    listener.invoke(Result.failure(QueryPurchasesFailedException(response)))
                }
                else -> {
                    listener.invoke(Result.failure(QueryPurchasesFailedException(response)))
                }
            }
        }
    }

    override fun queryPurchaseHistory(
        productType: ProductType,
        listener: ResponseListener<List<PurchaseHistoryRecord>>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        val params = QueryPurchaseHistoryParams.newBuilder()
            .setProductType(productType.rawValue)
            .build()

        /*        billingClient.queryPurchaseHistoryAsync(params) { result, purchases ->
                    when (val response = result.toResponse()) {
                        is BillingResponse.OK -> {
                            listener.invoke(Result.success(purchases.orEmpty()))
                        }
                        is BillingResponse.ServiceDisconnected, is BillingResponse.ServiceError -> {
                            Napier.d("queryPurchaseHistory: service error. CODE=${response.code}")
                            state = BillingClientProvider.State.DISPOSED
                            listener.invoke(Result.failure(QueryPurchasesFailedException(response)))
                        }
                        else -> {
                            listener.invoke(Result.failure(QueryPurchasesFailedException(response)))
                        }
                    }
                }*/
    }

    override fun consumePurchase(
        purchase: Purchase,
        listener: ResponseListener<ConsumeResult>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        val params = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(params) { result, _ ->
            when (val response = result.toResponse()) {
                is BillingResponse.OK -> {
                    listener.invoke(Result.success(ConsumeResult(false, params)))
                }
                is BillingResponse.ItemNotOwned -> {
                    listener.invoke(Result.success(ConsumeResult(true, params)))
                }
                is BillingResponse.ServiceDisconnected, is BillingResponse.ServiceError -> {
                    Napier.d("consumePurchase: service error. CODE=${response.code}")
                    state = BillingClientProvider.State.DISPOSED
                    listener.invoke(Result.failure(ConsumePurchaseFailedException(response, params)))
                }
                else -> {
                    listener.invoke(Result.failure(ConsumePurchaseFailedException(response, params)))
                }
            }
        }
    }

    override fun acknowledgePurchase(
        purchase: Purchase,
        listener: ResponseListener<AcknowledgeResult>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        val params = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(params) { result ->
            when (val response = result.toResponse()) {
                is BillingResponse.OK -> {
                    listener.invoke(Result.success(AcknowledgeResult(params)))
                }
                is BillingResponse.ServiceDisconnected, is BillingResponse.ServiceError -> {
                    Napier.d("acknowledgePurchase: service error. CODE=${response.code}")
                    state = BillingClientProvider.State.DISPOSED
                    listener.invoke(Result.failure(AcknowledgePurchaseFailedException(response, params)))
                }
                else -> {
                    listener.invoke(Result.failure(AcknowledgePurchaseFailedException(response, params)))
                }
            }
        }
    }

    override fun launchBillingFlow(
        activity: Activity,
        command: PurchaseSingleCommand,
        listener: ResponseListener<SingleBillingFlowResult>,
    ) {
        require(state == BillingClientProvider.State.CONNECTED) { "BillingClient is not connected" }

        val updatedListener = object : PurchasesUpdatedListener {
            override fun onPurchasesUpdated(result: BillingResult, purchases: MutableList<Purchase>?) {
                val response = result.toResponse()
                val isError = response !is BillingResponse.OK
                val isLibraryError = response is BillingResponse.OK && purchases == null
                val isPurchaseHandled = response is BillingResponse.OK && purchases != null && purchases.any {
                    it.products.contains(command.productId.value) && !it.isAcknowledged
                }

                if (isError || isLibraryError || isPurchaseHandled) {
                    compositeListener.remove(this)
                }

                when (response) {
                    is BillingResponse.OK -> {
                        if (purchases == null) {
                            listener.invoke(Result.failure(LaunchBillingFlowFailedException(response, command)))
                            return
                        }

                        if (purchases.any { it.products.contains(command.productId.value) && !it.isAcknowledged }) {
                            listener.invoke(Result.success(SingleBillingFlowResult(command, purchases)))
                        }
                    }
                    is BillingResponse.ServiceDisconnected, is BillingResponse.ServiceError -> {
                        Napier.d("launchBillingFlow: service error. CODE=${response.code}")
                        state = BillingClientProvider.State.DISPOSED
                        listener.invoke(Result.failure(LaunchBillingFlowFailedException(response, command)))
                    }
                    else -> {
                        listener.invoke(Result.failure(LaunchBillingFlowFailedException(response, command)))
                    }
                }
            }
        }

        compositeListener.add(updatedListener)

        val launchBillingResponse = billingClient
            .launchBillingFlow(activity, command.toBillingFlowParams())
            .toResponse()

        if (launchBillingResponse !is BillingResponse.OK) {
            if (launchBillingResponse is BillingResponse.ServiceDisconnected || launchBillingResponse is BillingResponse.ServiceError) {
                Napier.d("launchBillingFlow: service error. CODE=${launchBillingResponse.code}")
                state = BillingClientProvider.State.DISPOSED
            }

            compositeListener.remove(updatedListener)
            listener.invoke(Result.failure(LaunchBillingFlowFailedException(launchBillingResponse, command)))
        }
    }
}
