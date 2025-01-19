package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
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

    suspend fun get(): List<FanboxPost> {
        val map = cookiePreference.data.firstOrNull()?.asMap() ?: return emptyList()
        val values = map.values.mapNotNull {
            runCatching { Json.decodeFromString(FanboxPost.serializer(), it.toString()) }.getOrNull()
        }

        return values
    }

    private suspend fun notify() {
        _data.tryEmit(get().map { it.id })
    }
}
