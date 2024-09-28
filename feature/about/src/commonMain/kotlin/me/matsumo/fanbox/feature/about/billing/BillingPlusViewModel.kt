package me.matsumo.fanbox.feature.about.billing

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import coil3.PlatformContext
import kotlinx.coroutines.flow.StateFlow
import me.matsumo.fanbox.core.model.ScreenState

abstract class BillingPlusViewModel : ViewModel() {
    abstract val screenState: StateFlow<ScreenState<BillingPlusUiState>>

    abstract suspend fun purchase(context: PlatformContext, planType: BillingPlusUiState.Type): Boolean
    abstract suspend fun verify(context: PlatformContext): Boolean
    abstract suspend fun consume(context: PlatformContext): Boolean

    open fun cleanUp() {}
}

@Stable
data class BillingPlusUiState(
    val isPlusMode: Boolean = false,
    val isDeveloperMode: Boolean = false,
    val plans: List<Plan> = emptyList(),
    val formattedAnnualMonthlyPrice: String = "",
    val formattedAnnualDiscountRate: String = "",
) {
    @Stable
    data class Plan(
        val price: Long,
        val formattedPrice: String,
        val type: Type,
    )

    enum class Type {
        MONTHLY,
        YEARLY,
    }
}
