package me.matsumo.fanbox.feature.about.billing

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
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
import me.matsumo.fanbox.core.model.OfferCycle
import me.matsumo.fanbox.core.model.ScreenState
import me.matsumo.fanbox.core.model.Setting
import me.matsumo.fanbox.core.model.applyIntroOfferPeriod
import me.matsumo.fanbox.core.model.filterOfferedPlans
import me.matsumo.fanbox.core.model.hasIntroOffer
import me.matsumo.fanbox.core.model.resolveInitialSelectedPlanType
import me.matsumo.fanbox.core.model.resolveOfferCycle
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
    private val offeringId = MutableStateFlow<String?>(null)
    private val currentEpochSeconds = MutableStateFlow(nowEpochSeconds())
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
        currentEpochSeconds,
    ) { setting, plans, selectedPlanType, currentEpochSeconds ->
        val subscribedPlanType = setting.subscribedPlanType
        val introOfferCycle = resolveIntroOfferCycle(
            plans = plans,
            setting = setting,
            currentEpochSeconds = currentEpochSeconds,
        )
        val periodPlans = applyIntroOfferPeriod(plans, introOfferCycle?.isOfferPeriod == true)
        val offeredPlans = filterOfferedPlans(periodPlans, subscribedPlanType)

        if (offeredPlans.isNotEmpty()) {
            ScreenState.Idle(
                BillingUiState(
                    setting = setting,
                    plans = offeredPlans.toImmutableList(),
                    selectedPlanType = selectedPlanType,
                    subscribedPlanType = subscribedPlanType,
                    introOfferCycle = introOfferCycle,
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
            val billingPlans = suspendRunCatching { billingClient.getPlans() }.getOrNull()

            offeringId.value = billingPlans?.offeringId
            currentEpochSeconds.value = nowEpochSeconds()
            plans.value = billingPlans?.plans.orEmpty()
            selectInitialPlan()
            sendPaywallShownLog()
            updateCurrentTimeWhileIntroOffered()
        }
    }

    /** 導入価格のオファーがある間、提示の残り時間を表示できるよう、現在の時刻を 1 秒ごとに更新する。 */
    private suspend fun updateCurrentTimeWhileIntroOffered() {
        if (!hasIntroOffer(plans.value)) return

        while (true) {
            delay(1.seconds)
            currentEpochSeconds.value = nowEpochSeconds()
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

    private suspend fun selectInitialPlan() {
        val subscribedPlanType = settingRepository.setting.first().subscribedPlanType

        selectedPlanType.value = resolveInitialSelectedPlanType(
            requestedPlanType = selectedPlanType.value,
            offeredPlans = filterOfferedPlans(plans.value, subscribedPlanType),
            subscribedPlanType = subscribedPlanType,
        )
    }

    private suspend fun sendPaywallShownLog() {
        val setting = settingRepository.setting.first()
        val introOfferCycle = resolveIntroOfferCycle(
            plans = plans.value,
            setting = setting,
            currentEpochSeconds = currentEpochSeconds.value,
        )

        BillingLog.paywallShown(
            referrer = referrer,
            initialPlan = selectedPlanType.value.toLogValue(),
            isPlanLoaded = plans.value.isNotEmpty(),
            isTrialOffered = plans.value.any { plan -> plan.trialPeriod != null },
            daysSinceFirstLaunch = setting.daysSinceFirstLaunch(currentEpochSeconds.value),
            offeringId = offeringId.value.orEmpty(),
            isIntroOfferPeriod = introOfferCycle?.isOfferPeriod == true,
            introOfferCycle = introOfferCycle.toLogValue(),
        ).send()
        hasSentPaywallShownLog = true
    }

    private fun handleOnPlanSelected(type: BillingPlan.Type) {
        if (selectedPlanType.value == type) return
        if (isSubscribedPlan(type)) return

        selectedPlanType.value = type
        BillingLog.paywallPlanSelected(referrer, type.toLogValue()).send()
    }

    private fun handleOnPurchase() {
        // 購入処理の完了前に再度押されても、購入とログを二重に開始しない
        if (isPurchasing) return

        val planType = selectedPlanType.value

        // 契約中のプランは画面で購入できないが、状態の更新が遅れた場合に備えてここでも防ぐ
        if (isSubscribedPlan(planType)) return

        val uiState = (screenState.value as? ScreenState.Idle)?.data
        val hasTrial = plans.value.find { plan -> plan.type == planType }?.trialPeriod != null
        // 課金画面に表示している内容で購入する。提示の期間内の表示なら導入価格のオファーを、期間外の表示なら基本プランを購入する
        val isIntroOfferPeriod = uiState?.plans?.find { plan -> plan.type == planType }?.introOffer != null
        val offerLog = PurchaseOfferLog(
            offeringId = offeringId.value.orEmpty(),
            isIntroOfferPeriod = isIntroOfferPeriod,
            introOfferCycle = uiState?.introOfferCycle.toLogValue(),
        )

        isPurchasing = true
        lastPurchaseResult = PaywallPurchaseResult.PENDING
        BillingLog.purchaseStarted(
            referrer = referrer,
            plan = planType.toLogValue(),
            hasTrial = hasTrial,
            offeringId = offerLog.offeringId,
            isIntroOfferPeriod = offerLog.isIntroOfferPeriod,
            introOfferCycle = offerLog.introOfferCycle,
        ).send()

        viewModelScope.launch {
            val result = billingClient.purchase(planType, isIntroOfferPeriod)

            isPurchasing = false
            handlePurchaseResult(
                result = result,
                planType = planType,
                hasTrial = hasTrial,
                offerLog = offerLog,
            )
        }
    }

    private fun isSubscribedPlan(type: BillingPlan.Type): Boolean {
        val uiState = (screenState.value as? ScreenState.Idle)?.data

        return uiState?.subscribedPlanType == type
    }

    private suspend fun handlePurchaseResult(
        result: BillingPurchaseResult,
        planType: BillingPlan.Type,
        hasTrial: Boolean,
        offerLog: PurchaseOfferLog,
    ) {
        val plan = planType.toLogValue()

        when (result) {
            is BillingPurchaseResult.Success -> {
                lastPurchaseResult = PaywallPurchaseResult.PURCHASED
                BillingLog.purchaseSucceeded(
                    referrer = referrer,
                    plan = plan,
                    hasTrial = hasTrial,
                    offeringId = offerLog.offeringId,
                    isIntroOfferPeriod = offerLog.isIntroOfferPeriod,
                    introOfferCycle = offerLog.introOfferCycle,
                ).send()
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

/** 課金のログに送る、何回目の提示の周期か。導入価格のオファーがないときは -1 を送る。 */
private fun OfferCycle?.toLogValue(): Int = this?.number ?: -1

@OptIn(ExperimentalTime::class)
private fun nowEpochSeconds(): Long = Clock.System.now().epochSeconds

/**
 * 導入価格のオファーがあるときに、提示の周期での現在の位置を返す。
 *
 * 導入価格のオファーがないとき（無料トライアルの Offering など）と、初回起動の時刻が未記録のときは null を返す。
 */
private fun resolveIntroOfferCycle(
    plans: List<BillingPlan>,
    setting: Setting,
    currentEpochSeconds: Long,
): OfferCycle? {
    if (!hasIntroOffer(plans)) return null

    return resolveOfferCycle(setting.firstLaunchTime, currentEpochSeconds)
}

/** 購入を開始したときの、提示したオファーの内容。購入の開始と成立のログに同じ値を送る。 */
private data class PurchaseOfferLog(
    val offeringId: String,
    val isIntroOfferPeriod: Boolean,
    val introOfferCycle: Int,
)

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
    val subscribedPlanType: BillingPlan.Type?,
    val introOfferCycle: OfferCycle?,
)
