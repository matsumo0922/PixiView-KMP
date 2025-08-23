package me.matsumo.fanbox.feature.about.billing

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_toast_purchased_error
import me.matsumo.fanbox.core.resources.billing_plus_toast_verify_error
import me.matsumo.fanbox.core.resources.error_billing

class BillingViewModel(
    private val billingClient: BillingClient,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    private val plans = MutableStateFlow(emptyList<BillingPlan>())
    private val selectedPlanType = MutableStateFlow(BillingPlan.Type.MONTHLY)

    private val _messageEvent = Channel<BillingMessageEvent>(Channel.BUFFERED)
    val messageEvent = _messageEvent.receiveAsFlow()

    val screenState = combine(
        settingRepository.setting,
        plans,
        selectedPlanType,
    ) { setting, plans, selectedPlanType ->
        if (plans.isNotEmpty()) {
            ScreenState.Idle(
                BillingUiState(
                    setting = setting,
                    plans = plans.toImmutableList(),
                    selectedPlanType = selectedPlanType,
                )
            )
        } else {
            ScreenState.Error(Res.string.error_billing)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = ScreenState.Loading,
    )

    init {
        viewModelScope.launch {
            plans.value = suspendRunCatching { billingClient.getPlans() }.getOrNull().orEmpty()
        }
    }

    fun onViewEvent(event: BillingViewEvent) {
        when (event) {
            is BillingViewEvent.OnPlanSelected -> handleOnPlanSelected(event.type)
            BillingViewEvent.OnPurchaseClicked -> handleOnPurchase()
            BillingViewEvent.OnRestoreClicked -> handleRestore()
        }
    }

    private fun handleOnPlanSelected(type: BillingPlan.Type) {
        selectedPlanType.value = type
    }

    private fun handleOnPurchase() {
        viewModelScope.launch {
            val result = suspendRunCatching { billingClient.purchase(selectedPlanType.value) }.getOrElse { false }

            if (result) {
                settingRepository.setPlusMode(true)
                _messageEvent.trySend(BillingMessageEvent.Purchased)
            } else {
                _messageEvent.trySend(BillingMessageEvent.SnackBar(Res.string.billing_plus_toast_purchased_error))
            }
        }
    }

    private fun handleRestore() {
        viewModelScope.launch {
            val result = suspendRunCatching { billingClient.restore() }.getOrElse { false }

            if (result) {
                settingRepository.setPlusMode(true)
                _messageEvent.trySend(BillingMessageEvent.Purchased)
            } else {
                _messageEvent.trySend(BillingMessageEvent.SnackBar(Res.string.billing_plus_toast_verify_error))
            }
        }
    }
}

@Stable
data class BillingUiState(
    val setting: Setting,
    val plans: ImmutableList<BillingPlan>,
    val selectedPlanType: BillingPlan.Type,
)
