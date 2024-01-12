package me.matsumo.fanbox.feature.about.billing

import androidx.compose.runtime.Stable
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.repository.UserDataRepository
import moe.tlaster.precompose.viewmodel.ViewModel

class BillingPlusViewModel(
    /*private val billingClient: BillingClient,
    private val purchasePlusSubscriptionUseCase: PurchasePlusSubscriptionUseCase,
    private val consumePlusUseCase: ConsumePlusUseCase,
    private val verifyPlusUseCase: VerifyPlusUseCase,*/
    private val userDataRepository: UserDataRepository,
    private val ioDispatcher: CoroutineDispatcher,
) : ViewModel() {

    private var _screenState = MutableStateFlow<ScreenState<BillingPlusUiState>>(ScreenState.Loading)

    val screenState = _screenState.asStateFlow()

    init {
        /*viewModelScope.launch {
            _screenState.value = runCatching {
                val userData = userDataRepository.userData.firstOrNull()
                val productDetail = billingClient.queryProductDetails(ProductItem.plusSubscription, ProductType.SUBS)

                val plusSubscription = productDetail.rawProductDetails.subscriptionOfferDetails?.firstOrNull()
                val basePlanPricing = plusSubscription?.pricingPhases?.pricingPhaseList?.firstOrNull()

                BillingPlusUiState(
                    isPlusMode = userData?.isPlusMode ?: false,
                    isDeveloperMode = userData?.isDeveloperMode ?: false,
                    formattedPrice = basePlanPricing?.formattedPrice,
                    purchase = runCatching { verifyPlusUseCase.execute() }.getOrNull(),
                )
            }.fold(
                onSuccess = { ScreenState.Idle(it) },
                onFailure = {
                    Timber.w(it)
                    ScreenState.Error(
                        message = R.string.error_billing,
                        retryTitle = R.string.common_close,
                    )
                },
            )
        }*/
    }

    /*suspend fun purchase(activity: Activity): Boolean {
        return runCatching {
            withContext(ioDispatcher) {
                purchasePlusSubscriptionUseCase.execute(activity)
            }
        }.fold(
            onSuccess = {
                userDataRepository.setPlusMode(true)
                ToastUtil.show(activity, R.string.billing_plus_toast_purchased)
                true
            },
            onFailure = {
                Timber.w(it)
                ToastUtil.show(activity, R.string.billing_plus_toast_purchased_error)
                false
            },
        )
    }

    suspend fun verify(context: Context): Boolean {
        return runCatching {
            withContext(ioDispatcher) {
                verifyPlusUseCase.execute()
            }
        }.fold(
            onSuccess = {
                if (it != null) {
                    userDataRepository.setPlusMode(true)
                    ToastUtil.show(context, R.string.billing_plus_toast_verify)
                    true
                } else {
                    ToastUtil.show(context, R.string.billing_plus_toast_verify_error)
                    false
                }
            },
            onFailure = {
                Timber.w(it)
                ToastUtil.show(context, R.string.error_billing)
                false
            },
        )
    }

    suspend fun consume(context: Context, purchase: Purchase): Boolean {
        return runCatching {
            withContext(ioDispatcher) {
                consumePlusUseCase.execute(purchase)
            }
        }.fold(
            onSuccess = {
                userDataRepository.setPlusMode(false)
                ToastUtil.show(context, R.string.billing_plus_toast_consumed)
                true
            },
            onFailure = {
                Timber.w(it)
                ToastUtil.show(context, R.string.billing_plus_toast_consumed_error)
                false
            },
        )
    }*/
}

@Stable
data class BillingPlusUiState(
    val isPlusMode: Boolean = false,
    val isDeveloperMode: Boolean = false,
    val formattedPrice: String? = null,
    // val purchase: Purchase? = null,
)
