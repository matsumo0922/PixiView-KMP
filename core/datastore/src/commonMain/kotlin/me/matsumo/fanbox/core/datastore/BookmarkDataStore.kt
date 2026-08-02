package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId

class BookmarkDataStore(
    private val preferenceHelper: PreferenceHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val cookiePreference = preferenceHelper.create(PreferencesName.FANBOX_BOOKMARK)
    private val scope = CoroutineScope(ioDispatcher)

    private val _data = MutableSharedFlow<List<FanboxPostId>>(replay = 1)

    val data = _data.asSharedFlow()

    init {
        scope.launch {
            notify()
        }
    }

    suspend fun save(post: FanboxPost) {
        cookiePreference.edit {
            it[stringPreferencesKey(post.id.value)] = Json.encodeToString(FanboxPost.serializer(), post)
        }

        notify()
    }

    suspend fun remove(post: FanboxPost) {
        cookiePreference.edit {
            it.remove(stringPreferencesKey(post.id.value))
        }

        notify()
    }

    suspend fun clear() {
        cookiePreference.edit {
            it.clear()
        }

        notify()
    }

    /**
     * 保存済みのブックマークを読み出す。
     *
     * fankt 0.0.21 以前が書き込んだ JSON は ID を object として持つため、現在の serializer では
     * 読めない。読み出し時に新形式へ変換し、変換したものだけを書き戻す。変換できなかったエントリは
     * 件数をログに残した上で読み飛ばす。
     */
    suspend fun get(): List<FanboxPost> {
        val entries = cookiePreference.data.firstOrNull()?.asMap() ?: return emptyList()

        val posts = mutableListOf<FanboxPost>()
        val migrated = mutableMapOf<String, String>()
        var failureCount = 0

        for ((key, rawValue) in entries) {
            val json = rawValue.toString()

            when (val normalization = normalizeBookmarkIdJson(json)) {
                is BookmarkJsonNormalization.AlreadyCurrent -> Unit
                is BookmarkJsonNormalization.Migrated -> migrated[key.name] = normalization.json
                BookmarkJsonNormalization.Failure -> {
                    failureCount++
                    continue
                }
            }

            val post = migrateBookmarkJson(json)

            if (post != null) {
                posts += post
            } else {
                failureCount++
                migrated.remove(key.name)
            }
        }

        if (failureCount > 0) {
            Napier.e("Failed to restore $failureCount bookmark(s).")
        }

        if (migrated.isNotEmpty()) {
            writeBackMigrated(migrated)
        }

        return posts
    }

    /**
     * 旧形式から変換したエントリを書き戻す。
     *
     * [save] は書き込みのあとに [notify] を呼び、[notify] は [get] を呼ぶ。読み出しの途中でそれを
     * 使うと再帰するため、ここでは DataStore を直接 1 回だけ編集する。
     */
    private suspend fun writeBackMigrated(migrated: Map<String, String>) {
        runCatching {
            cookiePreference.edit { preferences ->
                for ((key, json) in migrated) {
                    preferences[stringPreferencesKey(key)] = json
                }
            }
        }.onFailure {
            // 書き戻しに失敗しても読み出した内容は返せる。次回の読み出しで再び変換される。
            Napier.e(it) { "Failed to write back ${migrated.size} migrated bookmark(s)." }
        }
    }

    private suspend fun notify() {
        _data.tryEmit(get().map { it.id })
    }
}
