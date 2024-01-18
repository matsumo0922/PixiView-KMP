package me.matsumo.fanbox.feature.about.billing

import coil3.PlatformContext
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.usecase.PurchasePlusSubscriptionUseCase
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.ui.MR
import moe.tlaster.precompose.viewmodel.viewModelScope
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle

class BillingPlusViewModelImpl(
    private val purchasePlusSubscriptionUseCase: PurchasePlusSubscriptionUseCase,
    private val userDataRepository: UserDataRepository,
    private val billingClient: BillingClient,
    private val ioDispatcher: CoroutineDispatcher,
): BillingPlusViewModel() {

    private var _screenState = MutableStateFlow<ScreenState<BillingPlusUiState>>(ScreenState.Loading)

    override val screenState = _screenState.asStateFlow()

    init {
        viewModelScope.launch {
            _screenState.value = suspendRunCatching {
                val userData =  userDataRepository.userData.firstOrNull()
                val product = billingClient.queryProductDetails(setOf("plus")).first()

                val numberFormat = NSNumberFormatter().apply {
                    numberStyle = NSNumberFormatterCurrencyStyle
                    locale = product.priceLocale
                }

                BillingPlusUiState(
                    isPlusMode = userData?.isPlusMode ?: false,
                    isDeveloperMode = userData?.isDeveloperMode ?: false,
                    formattedPrice = numberFormat.stringFromNumber(product.price) ?: "",
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
                purchasePlusSubscriptionUseCase.invoke()
            }
        }.onSuccess {
            userDataRepository.setPlusMode(true)
        }.isSuccess
    }

    override suspend fun consume(context: PlatformContext): Boolean {
        return false
    }

    override suspend fun verify(context: PlatformContext): Boolean {
        return false
    }
}
