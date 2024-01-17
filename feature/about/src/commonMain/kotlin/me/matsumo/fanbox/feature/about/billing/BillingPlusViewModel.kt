package me.matsumo.fanbox.feature.about.billing

import androidx.compose.runtime.Stable
import coil3.PlatformContext
import kotlinx.coroutines.flow.StateFlow
import me.matsumo.fanbox.core.model.ScreenState
import moe.tlaster.precompose.viewmodel.ViewModel

abstract class BillingPlusViewModel: ViewModel() {
    abstract val screenState: StateFlow<ScreenState<BillingPlusUiState>>

    abstract suspend fun purchase(context: PlatformContext): Boolean
    abstract suspend fun verify(context: PlatformContext): Boolean
    abstract suspend fun consume(context: PlatformContext): Boolean
}

@Stable
data class BillingPlusUiState(
    val isPlusMode: Boolean = false,
    val isDeveloperMode: Boolean = false,
    val formattedPrice: String? = null,
)
