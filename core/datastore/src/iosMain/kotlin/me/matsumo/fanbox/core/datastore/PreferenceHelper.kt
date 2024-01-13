@file:Suppress("MatchingDeclarationName", "Filename")

package me.matsumo.fanbox.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import okio.Path.Companion.toPath
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSFileManager
import platform.Foundation.NSUserDomainMask

class PreferenceHelperImpl(
    private val ioDispatcher: CoroutineDispatcher,
): PreferenceHelper {

    @OptIn(ExperimentalForeignApi::class)
    override fun create(name: String): DataStore<Preferences> {
        val documentDir = NSFileManager.defaultManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )

        return PreferenceDataStoreFactory.createWithPath(
            corruptionHandler = null,
            migrations = emptyList(),
            scope = CoroutineScope(ioDispatcher),
            produceFile = { "${documentDir!!.path}/$name.preferences_pb".toPath() }
        )
    }

    override fun delete(name: String) {
        // do nothing
    }
}
