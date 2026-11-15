package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 新着投稿の定期確認の状態を保存する。
 *
 * 前回までに確認した最新投稿の公開日時と、定期実行に登録した確認間隔（時間）を持つ。
 */
@OptIn(ExperimentalTime::class)
class NewPostCheckDataStore(
    preferenceHelper: PreferenceHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val preference = preferenceHelper.create(PreferencesName.NEW_POST_CHECK)
    private val lastSeenPublishedAtKey = longPreferencesKey("last_seen_published_at_millis")
    private val scheduledIntervalHoursKey = intPreferencesKey("scheduled_interval_hours")

    /** 前回までに確認した最新投稿の公開日時。まだ確認していなければ null を返す。 */
    suspend fun getLastSeenPublishedAt(): Instant? = withContext(ioDispatcher) {
        preference.data.first()[lastSeenPublishedAtKey]?.let(Instant::fromEpochMilliseconds)
    }

    suspend fun setLastSeenPublishedAt(publishedAt: Instant) = withContext(ioDispatcher) {
        preference.edit { it[lastSeenPublishedAtKey] = publishedAt.toEpochMilliseconds() }
    }

    /** 確認の記録を消す。次の確認は初回として扱われ、通知せずに記録だけ行う。 */
    suspend fun clearLastSeenPublishedAt() = withContext(ioDispatcher) {
        preference.edit { it.remove(lastSeenPublishedAtKey) }
    }

    /** 定期実行に登録した確認間隔（時間）。まだ登録していなければ null を返す。 */
    suspend fun getScheduledIntervalHours(): Int? = withContext(ioDispatcher) {
        preference.data.first()[scheduledIntervalHoursKey]
    }

    suspend fun setScheduledIntervalHours(intervalHours: Int) = withContext(ioDispatcher) {
        preference.edit { it[scheduledIntervalHoursKey] = intervalHours }
    }
}
