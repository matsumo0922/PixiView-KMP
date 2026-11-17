package me.matsumo.fanbox.core.model

import androidx.compose.runtime.Immutable
import kotlin.time.Duration
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours
import kotlin.time.Duration.Companion.seconds

/**
 * オファーの提示の周期での、ある時刻の位置。
 *
 * 周期は初回起動を起点とする 10 日ごとの区切りで、各周期の最初の 72 時間が提示の期間、残りの 7 日が休止の期間になる。
 *
 * @property number 何回目の周期か。初回起動の直後の周期を 1 とする
 * @property offerPeriodRemaining 提示の期間の残り時間。休止の期間は null
 */
@Immutable
data class OfferCycle(
    val number: Int,
    val offerPeriodRemaining: Duration?,
) {
    /** 提示の期間内かどうか。 */
    val isOfferPeriod get() = offerPeriodRemaining != null
}

/**
 * 初回起動の時刻 [firstLaunchTime] と現在の時刻 [currentEpochSeconds]（どちらも epoch 秒）から、オファーの提示の周期での位置を返す。
 *
 * 初回起動の時刻が未記録（負の値）なら null を返す。端末の時計が初回起動より前に戻った場合は、初回起動の時刻として扱う。
 */
fun resolveOfferCycle(firstLaunchTime: Long, currentEpochSeconds: Long): OfferCycle? {
    if (firstLaunchTime < 0L) return null

    val elapsed = (currentEpochSeconds - firstLaunchTime).coerceAtLeast(0L).seconds
    val cycleLength = 10.days
    val offerPeriodLength = 72.hours
    val cycleIndex = (elapsed.inWholeSeconds / cycleLength.inWholeSeconds).toInt()
    val elapsedInCycle = elapsed - cycleLength * cycleIndex

    val offerPeriodRemaining = (offerPeriodLength - elapsedInCycle).takeIf { remaining -> remaining.isPositive() }

    return OfferCycle(
        number = cycleIndex + 1,
        offerPeriodRemaining = offerPeriodRemaining,
    )
}
