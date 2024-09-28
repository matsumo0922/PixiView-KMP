package me.matsumo.fanbox.core.datastore

import android.content.Context
import kotlinx.coroutines.flow.first

class DummyDataStoreImpl(
    private val context: Context,
    private val userDataStore: PixiViewDataStore,
) : DummyDataStore {

    private val userData = userDataStore.userData

    override suspend fun setDummyData(key: String, value: String) {
        if (userData.first().isTestUser) {
            context.filesDir.resolve("$key.json").writeText(value)
        }
    }

    override suspend fun getDummyData(key: String): String? {
        val userData = userData.first()
        val file = context.resources.getIdentifier(key, "raw", context.packageName)

        if (!userData.isTestUser || file == 0) {
            return null
        }

        val inputStream = context.resources.openRawResource(file)

        return inputStream.bufferedReader().use { it.readText() }
    }
}
