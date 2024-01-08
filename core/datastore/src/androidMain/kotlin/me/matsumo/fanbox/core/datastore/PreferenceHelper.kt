package me.matsumo.fanbox.core.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toPath

class PreferenceHelperImpl(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher,
) : PreferenceHelper {

    override fun create(name: String): DataStore<Preferences> {
        val file = context.filesDir.resolve("$name.preferences_pb")

        return PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = null,
            migrations = emptyList(),
            scope = CoroutineScope(ioDispatcher),
            produceFile = { file.absolutePath.toPath() }
        )
    }

    override fun delete(name: String) {

    }
}
