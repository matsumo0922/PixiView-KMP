package me.matsumo.fanbox.core.datastore

class DummyDataStoreImpl : DummyDataStore {
    override suspend fun setDummyData(key: String, value: String) {
        // Dummy implementation
    }

    override suspend fun getDummyData(key: String): String? {
        // Dummy implementation
        return null
    }
}
