package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.datastore.FlagDataStore
import me.matsumo.fanbox.core.model.Flag

interface FlagRepository {
    suspend fun setFlag(key: Flag, value: Boolean)
    suspend fun getFlag(key: Flag, default: Boolean): Boolean
}

internal class FlagRepositoryImpl(
    private val flagDataStore: FlagDataStore,
) : FlagRepository {

    override suspend fun setFlag(key: Flag, value: Boolean) {
        flagDataStore.setFlag(key, value)
    }

    override suspend fun getFlag(key: Flag, default: Boolean): Boolean {
        return flagDataStore.getFlag(key, default)
    }
}
