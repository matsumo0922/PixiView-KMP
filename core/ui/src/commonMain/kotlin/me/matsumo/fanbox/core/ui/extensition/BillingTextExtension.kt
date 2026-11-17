package me.matsumo.fanbox.core.ui.extensition

import androidx.compose.runtime.Composable
import me.matsumo.fanbox.core.model.BillingIntroOffer
import me.matsumo.fanbox.core.model.BillingTrialPeriod
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.billing_plus_trial_period_day
import me.matsumo.fanbox.core.resources.billing_plus_trial_period_month
import me.matsumo.fanbox.core.resources.billing_plus_trial_period_week
import me.matsumo.fanbox.core.resources.billing_plus_trial_period_year
import me.matsumo.fanbox.core.resources.error_unknown
import org.jetbrains.compose.resources.StringResource
import org.jetbrains.compose.resources.stringResource
import kotlin.time.Duration

/** 期間の長さの表示用の文字列（「3日間」など）を返す。 */
@Composable
fun BillingTrialPeriod.getDisplayText(): String {
    if (unit == BillingTrialPeriod.Unit.UNKNOWN) {
        return stringResource(Res.string.error_unknown)
    }

    return stringResource(unit.toStringResource(), value)
}

/** 導入価格を適用する期間全体の長さの表示用の文字列（「3か月間」など）を返す。 */
@Composable
fun BillingIntroOffer.getTotalPeriodText(): String = period.copy(value = period.value * cycleCount).getDisplayText()

/** 提示の残り時間を「時間:分:秒」の形式で返す。72 時間の提示の期間を表せるよう、時間は 24 を超えても日に繰り上げない。 */
fun Duration.toCountdownText(): String = toComponents { hours, minutes, seconds, _ ->
    "$hours:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
}

private fun BillingTrialPeriod.Unit.toStringResource(): StringResource = when (this) {
    BillingTrialPeriod.Unit.DAY -> Res.string.billing_plus_trial_period_day
    BillingTrialPeriod.Unit.WEEK -> Res.string.billing_plus_trial_period_week
    BillingTrialPeriod.Unit.MONTH -> Res.string.billing_plus_trial_period_month
    BillingTrialPeriod.Unit.YEAR -> Res.string.billing_plus_trial_period_year
    BillingTrialPeriod.Unit.UNKNOWN -> Res.string.error_unknown
}
