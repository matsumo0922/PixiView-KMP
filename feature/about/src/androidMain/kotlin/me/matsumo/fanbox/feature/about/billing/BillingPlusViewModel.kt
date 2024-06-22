package me.matsumo.fanbox.feature.about.billing

import android.app.Activity
import androidx.lifecycle.viewModelScope
import coil3.PlatformContext
import com.android.billingclient.api.Purchase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.models.ProductItem
import me.matsumo.fanbox.core.billing.models.ProductType
import me.matsumo.fanbox.core.billing.usecase.ConsumePlusUseCase
import me.matsumo.fanbox.core.billing.usecase.PurchasePlusSubscriptionUseCase
import me.matsumo.fanbox.core.billing.usecase.VerifyPlusUseCase
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.MR

class BillingPlusViewModelImpl(
    private val billingClient: BillingClient,
    private val purchasePlusSubscriptionUseCase: PurchasePlusSubscriptionUseCase,
    private val consumePlusUseCase: ConsumePlusUseCase,
    private val verifyPlusUseCase: VerifyPlusUseCase,
    private val userDataRepository: UserDataRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : BillingPlusViewModel() {

    private var lastPurchase: Purchase? = null

    private var _screenState = MutableStateFlow<ScreenState<BillingPlusUiState>>(ScreenState.Loading)

    override val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            _screenState.value = runCatching {
                val userData = userDataRepository.userData.firstOrNull()
                val productDetail = billingClient.queryProductDetails(ProductItem.plusSubscription, ProductType.SUBS)

                val plans = productDetail.rawProductDetails.subscriptionOfferDetails?.map {
                    BillingPlusUiState.Plan(
                        price = it.pricingPhases.pricingPhaseList.first().priceAmountMicros.toInt() / 1000000,
                        formattedPrice = it.pricingPhases.pricingPhaseList.first().formattedPrice,
                        type = when (it.basePlanId) {
                            "plus" -> BillingPlusUiState.Type.MONTH
                            "plus-year" -> BillingPlusUiState.Type.YEAR
                            else -> BillingPlusUiState.Type.MONTH
                        },
                    )
                }

                lastPurchase = verifyPlusUseCase.invoke()

                BillingPlusUiState(
                    isPlusMode = userData?.isPlusMode ?: false,
                    isDeveloperMode = userData?.isDeveloperMode ?: false,
                    plans = plans.orEmpty(),
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = {
                    Napier.w(it) { "BillingPlusViewModelImpl" }
                    ScreenState.Error(
                        message = MR.strings.error_billing,
                        retryTitle = MR.strings.common_close,
                    )
                },
            )
        }
    }

    override suspend fun purchase(context: PlatformContext): Boolean {
        return runCatching {
            withContext(ioDispatcher) {
                purchasePlusSubscriptionUseCase.invoke(context as Activity)
            }
        }.onSuccess {
            userDataRepository.setPlusMode(true)
        }.isSuccess
    }

    override suspend fun verify(context: PlatformContext): Boolean {
        return runCatching {
            withContext(ioDispatcher) {
                verifyPlusUseCase.invoke()!!
            }
        }.onSuccess {
            userDataRepository.setPlusMode(true)
        }.isSuccess
    }

    override suspend fun consume(context: PlatformContext): Boolean {
        return runCatching {
            withContext(ioDispatcher) {
                consumePlusUseCase.invoke(lastPurchase!!)
            }
        }.onSuccess {
            userDataRepository.setPlusMode(false)
        }.isSuccess
    }
}
