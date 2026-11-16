package me.matsumo.fanbox.feature.service

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import me.matsumo.fanbox.core.datastore.NewContentCheckDataStore
import me.matsumo.fanbox.core.model.NewContentCheckConfig
import me.matsumo.fanbox.core.model.NewContentType
import kotlin.time.Duration.Companion.hours
import kotlin.time.toJavaDuration

/**
 * 新着投稿と新着メッセージを確かめる [NewContentCheckWorker] を、WorkManager の定期実行に登録・停止する。
 *
 * WorkManager は登録を端末に保存するため、端末を再起動しても、アプリを長期間開かなくても定期実行は続く。
 */
class NewContentCheckScheduler(
    context: Context,
    private val newContentCheckDataStore: NewContentCheckDataStore,
) {

    private val workManager = WorkManager.getInstance(context)

    /**
     * 新着投稿・新着メッセージの確認を行うかに合わせて、定期実行を登録・停止する。
     *
     * どちらかを行うなら登録し、登録済みのときは登録済みの間隔と次の実行時刻をそのまま残す。両方とも行わないなら止める。
     * 行わない確認は記録を消し、再び行うようにしたときの最初の確認を、通知せずに記録だけ行う初回にする。
     */
    suspend fun update(isPostCheckEnabled: Boolean, isMessageCheckEnabled: Boolean) {
        if (!isPostCheckEnabled) newContentCheckDataStore.clearLastSeenCreatedAt(NewContentType.POST)
        if (!isMessageCheckEnabled) newContentCheckDataStore.clearLastSeenCreatedAt(NewContentType.MESSAGE)

        if (isPostCheckEnabled || isMessageCheckEnabled) start() else workManager.cancelUniqueWork(WORK_NAME)
    }

    /** 定期実行の間隔を [intervalHours] 時間に変える。登録済みの間隔と同じときは何もしない。 */
    suspend fun updateInterval(intervalHours: Int) {
        if (newContentCheckDataStore.getScheduledIntervalHours() == intervalHours) return

        // 実行中の定期実行から呼ばれても、UPDATE は実行中の処理を止めずに次回以降の間隔を変える。
        enqueue(intervalHours, ExistingPeriodicWorkPolicy.UPDATE)
        newContentCheckDataStore.setScheduledIntervalHours(intervalHours)
    }

    private suspend fun start() {
        val intervalHours = newContentCheckDataStore.getScheduledIntervalHours() ?: NewContentCheckConfig.Default.intervalHours

        enqueue(intervalHours, ExistingPeriodicWorkPolicy.KEEP)
        newContentCheckDataStore.setScheduledIntervalHours(intervalHours)
    }

    private fun enqueue(intervalHours: Int, policy: ExistingPeriodicWorkPolicy) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<NewContentCheckWorker>(intervalHours.hours.toJavaDuration())
            .setConstraints(constraints)
            .build()

        workManager.enqueueUniquePeriodicWork(WORK_NAME, policy, request)
    }

    private companion object {
        const val WORK_NAME = "new_content_check"
    }
}
