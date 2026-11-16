package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.model.NewContentType
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * 新着の定期確認の状態を保存する。
 *
 * 新着投稿・新着メッセージの確認ごとに、前回までに確認した最新の作成日時（投稿は公開日時）と、記録の世代を別に持つ。
 * 定期実行に登録した確認間隔（時間）は、両方の確認で共有する。
 *
 * 記録の世代は、記録を消すたびに増える。確認を始めたときの世代と保存するときの世代を比べ、確認の途中で消された記録を書き戻さないようにする。
 */
@OptIn(ExperimentalTime::class)
class NewContentCheckDataStore(
    preferenceHelper: PreferenceHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val preference = preferenceHelper.create(PreferencesName.NEW_CONTENT_CHECK)
    private val scheduledIntervalHoursKey = intPreferencesKey("scheduled_interval_hours")

    /** 前回までに確認した最新の作成日時。まだ確認していなければ null を返す。 */
    suspend fun getLastSeenCreatedAt(type: NewContentType): Instant? = withContext(ioDispatcher) {
        preference.data.first()[lastSeenKeyOf(type)]?.let(Instant::fromEpochMilliseconds)
    }

    /** 記録の世代。確認を始めるときに読み、[setLastSeenCreatedAtIfGeneration] に渡す。 */
    suspend fun getRecordGeneration(type: NewContentType): Int = withContext(ioDispatcher) {
        preference.data.first()[generationKeyOf(type)] ?: 0
    }

    /**
     * 記録の世代が [generation] のままなら、前回までに確認した最新の作成日時を [createdAt] にして true を返す。
     *
     * 世代が変わっていれば（確認の途中で記録が消されていれば）保存せずに false を返す。世代の判定と保存は、1 回の更新の中で行う。
     */
    suspend fun setLastSeenCreatedAtIfGeneration(
        type: NewContentType,
        createdAt: Instant,
        generation: Int,
    ): Boolean = withContext(ioDispatcher) {
        var isSaved = false

        preference.edit { preferences ->
            if ((preferences[generationKeyOf(type)] ?: 0) != generation) return@edit

            preferences[lastSeenKeyOf(type)] = createdAt.toEpochMilliseconds()
            isSaved = true
        }

        isSaved
    }

    /** 確認の記録を消し、記録の世代を進める。次の確認は初回として扱われ、通知せずに記録だけ行う。 */
    suspend fun clearLastSeenCreatedAt(type: NewContentType) = withContext(ioDispatcher) {
        preference.edit { preferences ->
            preferences.remove(lastSeenKeyOf(type))
            preferences[generationKeyOf(type)] = (preferences[generationKeyOf(type)] ?: 0) + 1
        }
    }

    /** 定期実行に登録した確認間隔（時間）。まだ登録していなければ null を返す。 */
    suspend fun getScheduledIntervalHours(): Int? = withContext(ioDispatcher) {
        preference.data.first()[scheduledIntervalHoursKey]
    }

    suspend fun setScheduledIntervalHours(intervalHours: Int) = withContext(ioDispatcher) {
        preference.edit { it[scheduledIntervalHoursKey] = intervalHours }
    }

    private fun lastSeenKeyOf(type: NewContentType): Preferences.Key<Long> = when (type) {
        NewContentType.POST -> longPreferencesKey("last_seen_published_at_millis")
        NewContentType.MESSAGE -> longPreferencesKey("last_seen_message_created_at_millis")
    }

    private fun generationKeyOf(type: NewContentType): Preferences.Key<Int> = when (type) {
        NewContentType.POST -> intPreferencesKey("post_record_generation")
        NewContentType.MESSAGE -> intPreferencesKey("message_record_generation")
    }
}
