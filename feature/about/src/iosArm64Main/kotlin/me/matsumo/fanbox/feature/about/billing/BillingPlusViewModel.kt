package me.matsumo.fanbox.feature.about.billing

import androidx.lifecycle.viewModelScope
import coil3.PlatformContext
import io.github.aakira.napier.Napier
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import me.matsumo.fanbox.core.billing.swift.BillingController
import me.matsumo.fanbox.core.billing.swift.PlusProduct
import me.matsumo.fanbox.core.common.util.format
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.repository.UserDataRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_close
import me.matsumo.fanbox.core.resources.error_billing
import platform.Foundation.NSNumber
import platform.Foundation.NSNumberFormatter
import platform.Foundation.NSNumberFormatterCurrencyStyle
import kotlin.coroutines.resume

@OptIn(ExperimentalForeignApi::class)
class BillingPlusViewModelImpl(
    private val userDataRepository: UserDataRepository,
) : BillingPlusViewModel() {

    override val screenState = userDataRepository.userData.map { userData ->
        suspendRunCatching {
            callbackFlow {
                BillingController.queryProductsWithCompletionHandler { products ->
                    val result = runCatching {
                        val (monthlyProduct, yearlyProduct) = products!!
                            .filterIsInstance<PlusProduct>()
                            .sortedBy { it.price().toLong() }

                        val monthly = monthlyProduct.toPlan(BillingPlusUiState.Type.MONTHLY)
                        val yearly = yearlyProduct.toPlan(BillingPlusUiState.Type.YEARLY)

                        val yearlyMonthlyPrice = yearly.price / 12

                        BillingPlusUiState(
                            isPlusMode = userData.isPlusMode,
                            isDeveloperMode = userData.isDeveloperMode,
                            plans = listOf(monthly, yearly),
                            formattedAnnualMonthlyPrice = formatCurrency(yearlyMonthlyPrice),
                            formattedAnnualDiscountRate = "%d".format(((monthly.price - yearlyMonthlyPrice) / monthly.price.toDouble() * 100).toInt()) + "%",
                        )
                    }

                    trySend(result.getOrThrow())
                }

                awaitClose {
                    // do nothing
                }
            }.first()
        }.fold(
            onSuccess = { ScreenState.Idle(it) },
            onFailure = {
                ScreenState.Error(
                    message = Res.string.error_billing,
                    retryTitle = Res.string.common_close,
                )
            },
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    override suspend fun purchase(context: PlatformContext, planType: BillingPlusUiState.Type): Boolean = suspendCancellableCoroutine { continuation ->
        val planId = when (planType) {
            BillingPlusUiState.Type.MONTHLY -> "plus"
            BillingPlusUiState.Type.YEARLY -> "plus_year"
        }

        BillingController.purchaseWithId(
            id = planId,
            onResult = {
                viewModelScope.launch {
                    val isPurchased = (it.toInt() == 0)

                    Napier.d { "isPurchased: $isPurchased" }

                    userDataRepository.setPlusMode(isPurchased)
                    continuation.resume(isPurchased)
                }
            },
            completionHandler = {
                // do nothing
            },
        )
    }

    override suspend fun consume(context: PlatformContext): Boolean {
        return false
    }

    override suspend fun verify(context: PlatformContext): Boolean = suspendCancellableCoroutine { continuation ->
        BillingController.refreshOnResult(
            onResult = {
                viewModelScope.launch {
                    Napier.d { "isPurchased: $it" }

                    userDataRepository.setPlusMode(it)
                    continuation.resume(it)
                }
            },
            completionHandler = {
                // do nothing
            },
        )
    }

    private fun PlusProduct.toPlan(type: BillingPlusUiState.Type): BillingPlusUiState.Plan {
        return BillingPlusUiState.Plan(
            price = price().toLong(),
            formattedPrice = formattedPrice(),
            type = type,
        )
    }

    private fun formatCurrency(amount: Long): String {
        val formatter = NSNumberFormatter().apply {
            numberStyle = NSNumberFormatterCurrencyStyle
        }

        return formatter.stringFromNumber(NSNumber(long = amount)) ?: "$amount"
    }
}
