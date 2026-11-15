package me.matsumo.fanbox.core.ui.ads

import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.minutes

/** ロード済みの広告を有効とみなす最大保持時間。AdMob の広告失効（ロードから 1 時間）より短く設定する。 */
internal val AD_EXPIRATION_DURATION = 50.minutes

/** elapsedRealtime 基準のロード時刻と現在時刻から、ロード済みの広告が失効しているかを判定する。 */
internal fun isAdExpired(loadedAtElapsedRealtime: Long, currentElapsedRealtime: Long): Boolean {
    val elapsedSinceLoaded = (currentElapsedRealtime - loadedAtElapsedRealtime).milliseconds

    return elapsedSinceLoaded >= AD_EXPIRATION_DURATION
}
