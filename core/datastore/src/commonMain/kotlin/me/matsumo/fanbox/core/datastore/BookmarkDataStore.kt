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
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.PostId

class BookmarkDataStore(
    private val preferenceHelper: PreferenceHelper,
    private val ioDispatcher: CoroutineDispatcher,
) {
    private val cookiePreference = preferenceHelper.create(PreferencesName.FANBOX_COOKIE)
    private val scope = CoroutineScope(ioDispatcher)

    private val _data = MutableSharedFlow<List<PostId>>(replay = 1)

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
        return cookiePreference.data.firstOrNull()?.asMap()?.values?.map {
            Json.decodeFromString(FanboxPost.serializer(), it.toString())
        } ?: emptyList()
    }

    private suspend fun notify() {
        _data.tryEmit(get().map { it.id })
    }
}
