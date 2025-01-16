package me.matsumo.fanbox.core.datastore

import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId

class BlockDataStore(
    preferenceHelper: PreferenceHelper,
    ioDispatcher: CoroutineDispatcher,
) {
    private val cookiePreference = preferenceHelper.create(PreferencesName.FANBOX_BLOCK)
    private val scope = CoroutineScope(ioDispatcher)

    private val _data = MutableSharedFlow<Set<FanboxCreatorId>>(replay = 1)

    val data = _data.asSharedFlow()

    init {
        scope.launch {
            notify()
        }
    }

    suspend fun blockCreator(creatorId: FanboxCreatorId) {
        cookiePreference.edit {
            it[stringSetPreferencesKey(BLOCKED_CREATOR)] = fetchBlockedCreators().plus(creatorId.value)
        }

        notify()
    }

    suspend fun unblockCreator(creatorId: FanboxCreatorId) {
        cookiePreference.edit {
            it[stringSetPreferencesKey(BLOCKED_CREATOR)] = fetchBlockedCreators().minus(creatorId.value)
        }

        notify()
    }

    suspend fun clear() {
        cookiePreference.edit {
            it.clear()
        }

        notify()
    }

    private suspend fun fetchBlockedCreators(): Set<String> {
        return cookiePreference.data.firstOrNull()?.let { it[stringSetPreferencesKey(BLOCKED_CREATOR)] } ?: setOf()
    }

    private suspend fun notify() {
        _data.emit(fetchBlockedCreators().map { FanboxCreatorId(it) }.toSet())
    }

    companion object {
        const val BLOCKED_CREATOR = "blocked_creator"
    }
}
