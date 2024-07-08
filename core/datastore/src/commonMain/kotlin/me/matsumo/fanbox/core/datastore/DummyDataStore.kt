package me.matsumo.fanbox.core.datastore

interface DummyDataStore {
    suspend fun setDummyData(key: String, value: String)
    suspend fun getDummyData(key: String): String?
}