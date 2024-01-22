package me.matsumo.fanbox.feature.about.billing

import coil3.PlatformContext
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

    override val screenState = userDataRepository.userData.map {
        suspendRunCatching {
            val product = billingClient.queryProductDetails(setOf("plus")).first()
            val numberFormat = NSNumberFormatter().apply {
                numberStyle = NSNumberFormatterCurrencyStyle
                locale = product.priceLocale
            }

            BillingPlusUiState(
                isPlusMode = it.isPlusMode,
                isDeveloperMode = it.isDeveloperMode,
                formattedPrice = numberFormat.stringFromNumber(product.price) ?: "",
            )
        }.fold(
            onSuccess = { ScreenState.Idle(it) },
            onFailure = {
                ScreenState.Error(
                    message = MR.strings.error_billing,
                    retryTitle = MR.strings.common_close,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    override suspend fun purchase(context: PlatformContext): Boolean {
        runCatching {
            withContext(ioDispatcher) {
                purchasePlusSubscriptionUseCase.invoke()
            }
        }

        return false
    }

    override suspend fun consume(context: PlatformContext): Boolean {
        return false
    }

    override suspend fun verify(context: PlatformContext): Boolean {
        return false
    }
}
