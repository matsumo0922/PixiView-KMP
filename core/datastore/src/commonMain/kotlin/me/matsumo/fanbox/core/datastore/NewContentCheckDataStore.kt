package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 新着の定期確認の状態を保存する。
 *
 * 前回までに確認した最新投稿の公開日時と、最新メッセージの作成日時を、それぞれ別に持つ。定期実行に登録した確認間隔（時間）は、両方の確認で共有する。
 */
@OptIn(ExperimentalTime::class)
class NewContentCheckDataStore(
    preferenceHelper: PreferenceHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val preference = preferenceHelper.create(PreferencesName.NEW_CONTENT_CHECK)
    private val lastSeenPublishedAtKey = longPreferencesKey("last_seen_published_at_millis")
    private val lastSeenMessageCreatedAtKey = longPreferencesKey("last_seen_message_created_at_millis")
    private val scheduledIntervalHoursKey = intPreferencesKey("scheduled_interval_hours")

    /** 前回までに確認した最新投稿の公開日時。まだ確認していなければ null を返す。 */
    suspend fun getLastSeenPublishedAt(): Instant? = getInstant(lastSeenPublishedAtKey)

    suspend fun setLastSeenPublishedAt(publishedAt: Instant) = setInstant(lastSeenPublishedAtKey, publishedAt)

    /** 新着投稿の確認の記録を消す。次の確認は初回として扱われ、通知せずに記録だけ行う。 */
    suspend fun clearLastSeenPublishedAt() = clear(lastSeenPublishedAtKey)

    /** 前回までに確認した最新メッセージの作成日時。まだ確認していなければ null を返す。 */
    suspend fun getLastSeenMessageCreatedAt(): Instant? = getInstant(lastSeenMessageCreatedAtKey)

    suspend fun setLastSeenMessageCreatedAt(createdAt: Instant) = setInstant(lastSeenMessageCreatedAtKey, createdAt)

    /** 新着メッセージの確認の記録を消す。次の確認は初回として扱われ、通知せずに記録だけ行う。 */
    suspend fun clearLastSeenMessageCreatedAt() = clear(lastSeenMessageCreatedAtKey)

    /** 定期実行に登録した確認間隔（時間）。まだ登録していなければ null を返す。 */
    suspend fun getScheduledIntervalHours(): Int? = withContext(ioDispatcher) {
        preference.data.first()[scheduledIntervalHoursKey]
    }

    suspend fun setScheduledIntervalHours(intervalHours: Int) = withContext(ioDispatcher) {
        preference.edit { it[scheduledIntervalHoursKey] = intervalHours }
    }

    private suspend fun getInstant(key: Preferences.Key<Long>): Instant? = withContext(ioDispatcher) {
        preference.data.first()[key]?.let(Instant::fromEpochMilliseconds)
    }

    private suspend fun setInstant(key: Preferences.Key<Long>, instant: Instant) = withContext(ioDispatcher) {
        preference.edit { it[key] = instant.toEpochMilliseconds() }
    }

    private suspend fun clear(key: Preferences.Key<Long>) = withContext(ioDispatcher) {
        preference.edit { it.remove(key) }
    }
}
