package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** Plus 課金状態の Setting 更新差分を検証するテスト。 */
class SettingPlusStatusUpdateTest {

    @Test
    fun fromKeepsSetToCancelStatusWhenActiveSubscriptionWillNotRenew() {
        val plusStatus = BillingPlusStatus(
            isActive = true,
            isTrial = false,
            willRenew = false,
            unsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
            planType = BillingPlan.Type.MONTHLY,
        )

        val update = SettingPlusStatusUpdate.from(Setting.default(), plusStatus)

        assertTrue(update.isPlusMode)
        assertFalse(update.isPlusTrial)
        assertTrue(update.isPlusSubscriptionSetToCancel)
        assertEquals(UNSUBSCRIBE_DETECTED_AT_MILLIS, update.plusUnsubscribeDetectedAtMillis)
        assertEquals(BillingPlan.Type.MONTHLY, update.plusPlanType)
        assertTrue(update.hasChanged)
    }

    @Test
    fun fromClearsSetToCancelStatusWhenSubscriptionWillRenewAgain() {
        val currentSetting = Setting.default().copy(
            isPlusMode = true,
            isPlusSubscriptionSetToCancel = true,
            plusUnsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
            plusPlanType = BillingPlan.Type.ANNUAL,
            plusRetentionPromptLastShownAtMillis = LAST_SHOWN_AT_MILLIS,
            plusRetentionPromptLastShownUnsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
        )
        val plusStatus = BillingPlusStatus(
            isActive = true,
            isTrial = false,
            willRenew = true,
            unsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
            planType = BillingPlan.Type.ANNUAL,
        )

        val update = SettingPlusStatusUpdate.from(currentSetting, plusStatus)

        assertTrue(update.isPlusMode)
        assertFalse(update.isPlusSubscriptionSetToCancel)
        assertEquals(null, update.plusUnsubscribeDetectedAtMillis)
        assertEquals(BillingPlan.Type.ANNUAL, update.plusPlanType)
        assertTrue(update.hasChanged)
    }

    @Test
    fun fromClearsCurrentSubscriptionStateWhenPlusExpired() {
        val currentSetting = Setting.default().copy(
            isPlusMode = true,
            isPlusSubscriptionSetToCancel = true,
            plusUnsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
            plusPlanType = BillingPlan.Type.ANNUAL,
            plusRetentionPromptLastShownAtMillis = LAST_SHOWN_AT_MILLIS,
            plusRetentionPromptLastShownUnsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
        )
        val plusStatus = BillingPlusStatus(
            isActive = false,
            isTrial = false,
            willRenew = false,
            unsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
            planType = BillingPlan.Type.ANNUAL,
        )

        val update = SettingPlusStatusUpdate.from(currentSetting, plusStatus)

        assertFalse(update.isPlusMode)
        assertFalse(update.isPlusSubscriptionSetToCancel)
        assertEquals(null, update.plusUnsubscribeDetectedAtMillis)
        assertEquals(BillingPlan.Type.UNKNOWN, update.plusPlanType)
        assertTrue(update.hasChanged)
    }

    /** テストで利用する固定値をまとめるオブジェクト。 */
    private companion object {
        /** テストで利用する解約検知時刻。 */
        private const val UNSUBSCRIBE_DETECTED_AT_MILLIS = 1_000L

        /** テストで利用する表示済み時刻。 */
        private const val LAST_SHOWN_AT_MILLIS = 10_000L
    }
}
