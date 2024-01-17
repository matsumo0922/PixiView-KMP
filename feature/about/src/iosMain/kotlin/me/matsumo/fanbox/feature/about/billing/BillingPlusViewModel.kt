package me.matsumo.fanbox.feature.about.billing

import coil3.PlatformContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import me.matsumo.fanbox.core.model.ScreenState

class BillingPlusViewModelImpl: BillingPlusViewModel() {

    override val screenState: StateFlow<ScreenState<BillingPlusUiState>> = MutableStateFlow(ScreenState.Loading)

    override suspend fun purchase(context: PlatformContext): Boolean {
        return false
    }

    override suspend fun consume(context: PlatformContext): Boolean {
        return false
    }

    override suspend fun verify(context: PlatformContext): Boolean {
        return false
    }
}
