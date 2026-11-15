package me.matsumo.fanbox.feature.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.matsumo.fanbox.core.datastore.NewPostCheckDataStore
import me.matsumo.fanbox.core.model.NewPostCheckConfig
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

/**
 * 支援中・フォロー中のクリエイターの新着投稿を確かめる [NewPostCheckWorker] を、WorkManager の定期実行に登録・停止する。
 *
 * WorkManager は登録を端末に保存するため、端末を再起動しても、アプリを長期間開かなくても定期実行は続く。
 */
class NewPostCheckScheduler(
    context: Context,
    private val newPostCheckDataStore: NewPostCheckDataStore,
) {

    private val workManager = WorkManager.getInstance(context)

    /** 定期実行を登録する。登録済みのときは、登録済みの間隔と次の実行時刻をそのまま残す。 */
    suspend fun start() {
        val intervalHours = newPostCheckDataStore.getScheduledIntervalHours() ?: NewPostCheckConfig.Default.intervalHours

        enqueue(intervalHours, ExistingPeriodicWorkPolicy.KEEP)
        newPostCheckDataStore.setScheduledIntervalHours(intervalHours)
    }

    /** 定期実行の間隔を [intervalHours] 時間に変える。登録済みの間隔と同じときは何もしない。 */
    suspend fun updateInterval(intervalHours: Int) {
        if (newPostCheckDataStore.getScheduledIntervalHours() == intervalHours) return

        // 実行中の定期実行から呼ばれても、UPDATE は実行中の処理を止めずに次回以降の間隔を変える。
        enqueue(intervalHours, ExistingPeriodicWorkPolicy.UPDATE)
        newPostCheckDataStore.setScheduledIntervalHours(intervalHours)
    }

    /** 定期実行を止め、確認の記録を消す。再び登録したときの最初の確認は、通知せずに記録だけ行う。 */
    suspend fun stop() {
        workManager.cancelUniqueWork(WORK_NAME)
        newPostCheckDataStore.clearLastSeenPublishedAt()
    }

    private fun enqueue(intervalHours: Int, policy: ExistingPeriodicWorkPolicy) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<NewPostCheckWorker>(intervalHours.hours.toJavaDuration())
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }

    private companion object {
        const val WORK_NAME = "new_post_check"
    }
}
