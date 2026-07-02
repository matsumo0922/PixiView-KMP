package me.matsumo.fanbox.core.model

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/** 解約予約ユーザー向けリテンション UI の表示判定を検証するテスト。 */
class SettingBillingRetentionPromptTest {

    @Test
    fun canShowBillingRetentionPromptReturnsTrueWhenPromptHasNeverBeenShown() {
        val setting = setToCancelSetting()

        assertTrue(setting.canShowBillingRetentionPrompt(CURRENT_TIME_MILLIS))
    }

    @Test
    fun canShowBillingRetentionPromptReturnsFalseBeforeOneDayInterval() {
        val setting = setToCancelSetting().copy(
            plusRetentionPromptLastShownAtMillis = CURRENT_TIME_MILLIS,
            plusRetentionPromptLastShownUnsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
        )

        val nextTimeMillis = CURRENT_TIME_MILLIS + ONE_DAY_MILLIS - 1L

        assertFalse(setting.canShowBillingRetentionPrompt(nextTimeMillis))
    }

    @Test
    fun canShowBillingRetentionPromptReturnsTrueAfterOneDayInterval() {
        val setting = setToCancelSetting().copy(
            plusRetentionPromptLastShownAtMillis = CURRENT_TIME_MILLIS,
            plusRetentionPromptLastShownUnsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
        )

        val nextTimeMillis = CURRENT_TIME_MILLIS + ONE_DAY_MILLIS

        assertTrue(setting.canShowBillingRetentionPrompt(nextTimeMillis))
    }

    @Test
    fun canShowBillingRetentionPromptReturnsTrueWhenUnsubscribeEpisodeChanged() {
        val setting = setToCancelSetting().copy(
            plusUnsubscribeDetectedAtMillis = NEW_UNSUBSCRIBE_DETECTED_AT_MILLIS,
            plusRetentionPromptLastShownAtMillis = CURRENT_TIME_MILLIS,
            plusRetentionPromptLastShownUnsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
        )

        assertTrue(setting.canShowBillingRetentionPrompt(CURRENT_TIME_MILLIS))
    }

    @Test
    fun canShowBillingRetentionPromptReturnsFalseWhenSubscriptionIsNotSetToCancel() {
        val setting = setToCancelSetting().copy(
            isPlusSubscriptionSetToCancel = false,
        )

        assertFalse(setting.canShowBillingRetentionPrompt(CURRENT_TIME_MILLIS))
    }

    @Test
    fun shouldShowBillingRetentionAnnualOfferReturnsFalseOnlyForAnnualPlan() {
        val annualSetting = setToCancelSetting().copy(
            plusPlanType = BillingPlan.Type.ANNUAL,
        )
        val monthlySetting = setToCancelSetting().copy(
            plusPlanType = BillingPlan.Type.MONTHLY,
        )
        val unknownSetting = setToCancelSetting().copy(
            plusPlanType = BillingPlan.Type.UNKNOWN,
        )

        assertFalse(annualSetting.shouldShowBillingRetentionAnnualOffer)
        assertTrue(monthlySetting.shouldShowBillingRetentionAnnualOffer)
        assertTrue(unknownSetting.shouldShowBillingRetentionAnnualOffer)
    }

    private fun setToCancelSetting(): Setting {
        return Setting.default().copy(
            isPlusMode = true,
            isPlusSubscriptionSetToCancel = true,
            plusUnsubscribeDetectedAtMillis = UNSUBSCRIBE_DETECTED_AT_MILLIS,
            plusPlanType = BillingPlan.Type.MONTHLY,
        )
    }

    /** テストで利用する固定値をまとめるオブジェクト。 */
    private companion object {
        /** 現在時刻として扱うミリ秒。 */
        private const val CURRENT_TIME_MILLIS = 10_000L

        /** 1 日分のミリ秒。 */
        private const val ONE_DAY_MILLIS = 86_400_000L

        /** テストで利用する初回の解約検知時刻。 */
        private const val UNSUBSCRIBE_DETECTED_AT_MILLIS = 1_000L

        /** テストで利用する別エピソードの解約検知時刻。 */
        private const val NEW_UNSUBSCRIBE_DETECTED_AT_MILLIS = 2_000L
    }
}
