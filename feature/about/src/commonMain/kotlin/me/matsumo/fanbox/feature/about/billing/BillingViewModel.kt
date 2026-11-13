package me.matsumo.fanbox.feature.about.billing

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.WhileSubscribed
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.billing.BillingClient
import me.matsumo.fanbox.core.billing.BillingPurchaseResult
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.logs.category.BillingLog
import me.matsumo.fanbox.core.logs.logger.send
import me.matsumo.fanbox.core.model.BillingPlan
import me.matsumo.fanbox.core.model.BillingPlusStatus
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.repository.SettingRepository
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_toast_purchased_error
import me.matsumo.fanbox.core.resources.billing_plus_toast_verify_error
import me.matsumo.fanbox.core.resources.error_billing
import kotlin.time.Clock
import kotlin.time.Duration.Companion.seconds
import kotlin.time.ExperimentalTime

class BillingViewModel(
    destination: Destination.BillingPlusBottomSheet,
    private val billingClient: BillingClient,
    private val settingRepository: SettingRepository,
) : ViewModel() {

    private val referrer = destination.referrer.logValue
    private val plans = MutableStateFlow(emptyList<BillingPlan>())
    private val selectedPlanType = MutableStateFlow(destination.initialPlanType.toInitialPlanType())
    private var isPurchasing = false
    private var hasSentPaywallShownLog = false
    private var lastPurchaseResult = PaywallPurchaseResult.NONE

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
                ),
            )
        } else {
            ScreenState.Error(Res.string.error_billing)
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(stopTimeout = 5.seconds),
        initialValue = ScreenState.Loading,
    )

    init {
        viewModelScope.launch {
            plans.value = suspendRunCatching { billingClient.getPlans() }.getOrNull().orEmpty()
            sendPaywallShownLog()
        }
    }

    override fun onCleared() {
        // 表示のログより前に閉じた場合は送らず、表示から閉じるまでの件数を揃える
        val shouldSendDismissedLog = hasSentPaywallShownLog && lastPurchaseResult != PaywallPurchaseResult.PURCHASED
        if (!shouldSendDismissedLog) return

        BillingLog.paywallDismissed(referrer, lastPurchaseResult.logValue).send()
    }

    fun onViewEvent(event: BillingViewEvent) {
        when (event) {
            is BillingViewEvent.OnPlanSelected -> handleOnPlanSelected(event.type)
            BillingViewEvent.OnPurchaseClicked -> handleOnPurchase()
            BillingViewEvent.OnRestoreClicked -> handleRestore()
        }
    }

    @OptIn(ExperimentalTime::class)
    private suspend fun sendPaywallShownLog() {
        val setting = settingRepository.setting.first()

        BillingLog.paywallShown(
            referrer = referrer,
            initialPlan = selectedPlanType.value.toLogValue(),
            isPlanLoaded = plans.value.isNotEmpty(),
            isTrialOffered = plans.value.any { plan -> plan.trialPeriod != null },
            daysSinceFirstLaunch = setting.daysSinceFirstLaunch(Clock.System.now().epochSeconds),
        ).send()
        hasSentPaywallShownLog = true
    }

    private fun handleOnPlanSelected(type: BillingPlan.Type) {
        if (selectedPlanType.value == type) return

        selectedPlanType.value = type
        BillingLog.paywallPlanSelected(referrer, type.toLogValue()).send()
    }

    private fun handleOnPurchase() {
        // 購入処理の完了前に再度押されても、購入とログを二重に開始しない
        if (isPurchasing) return

        val planType = selectedPlanType.value
        val hasTrial = plans.value.find { plan -> plan.type == planType }?.trialPeriod != null

        isPurchasing = true
        lastPurchaseResult = PaywallPurchaseResult.PENDING
        BillingLog.purchaseStarted(referrer, planType.toLogValue(), hasTrial).send()

        viewModelScope.launch {
            val result = billingClient.purchase(planType)

            isPurchasing = false
            handlePurchaseResult(
                result = result,
                planType = planType,
                hasTrial = hasTrial,
            )
        }
    }

    private suspend fun handlePurchaseResult(
        result: BillingPurchaseResult,
        planType: BillingPlan.Type,
        hasTrial: Boolean,
    ) {
        val plan = planType.toLogValue()

        when (result) {
            is BillingPurchaseResult.Success -> {
                lastPurchaseResult = PaywallPurchaseResult.PURCHASED
                BillingLog.purchaseSucceeded(referrer, plan, hasTrial).send()
                settingRepository.setPlusStatus(result.status)
                _messageEvent.trySend(BillingMessageEvent.Purchased)
            }

            BillingPurchaseResult.Cancelled -> {
                lastPurchaseResult = PaywallPurchaseResult.CANCELLED
                BillingLog.purchaseCancelled(referrer, plan).send()
                _messageEvent.trySend(BillingMessageEvent.SnackBar(Res.string.billing_plus_toast_purchased_error))
            }

            is BillingPurchaseResult.Failure -> {
                lastPurchaseResult = PaywallPurchaseResult.FAILED
                BillingLog.purchaseFailed(referrer, plan, result.errorType).send()
                _messageEvent.trySend(BillingMessageEvent.SnackBar(Res.string.billing_plus_toast_purchased_error))
            }
        }
    }

    private fun handleRestore() {
        viewModelScope.launch {
            val result = suspendRunCatching { billingClient.restore() }.getOrElse { inactivePlusStatus() }

            if (result.isActive) {
                lastPurchaseResult = PaywallPurchaseResult.RESTORED
                settingRepository.setPlusStatus(result)
                _messageEvent.trySend(BillingMessageEvent.Purchased)
            } else {
                _messageEvent.trySend(BillingMessageEvent.SnackBar(Res.string.billing_plus_toast_verify_error))
            }
        }
    }
}

/** 課金画面のログに送るプランの値。 */
private fun BillingPlan.Type.toLogValue(): String = name.lowercase()

/** 課金画面を開いたときに選択しておくプラン。指定がなければ月額にする。 */
private fun BillingPlan.Type?.toInitialPlanType(): BillingPlan.Type = when (this) {
    null, BillingPlan.Type.UNKNOWN -> BillingPlan.Type.MONTHLY
    else -> this
}

/** 課金画面を閉じるまでの最後の購入の結果。課金画面を閉じたときのログに [logValue] を送る。 */
private enum class PaywallPurchaseResult(val logValue: String) {
    NONE("none"),
    PENDING("pending"),
    CANCELLED("cancelled"),
    FAILED("failed"),
    RESTORED("restored"),
    PURCHASED("purchased"),
}

private fun inactivePlusStatus(): BillingPlusStatus = BillingPlusStatus(
    isActive = false,
    isTrial = false,
    willRenew = false,
    unsubscribeDetectedAtMillis = null,
    planType = BillingPlan.Type.UNKNOWN,
)

/** Plus 課金画面の UI 状態を表すモデル。 */
@Stable
data class BillingUiState(
    val setting: Setting,
    val plans: ImmutableList<BillingPlan>,
    val selectedPlanType: BillingPlan.Type,
)
