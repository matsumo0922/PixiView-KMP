package me.matsumo.fanbox.core.datastore

import android.content.Context
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.resources.Res
import org.jetbrains.compose.resources.ExperimentalResourceApi

class DummyDataStoreImpl(
    private val context: Context,
    private val userDataStore: SettingDataStore,
) : DummyDataStore {

    private val userData = userDataStore.setting

    override suspend fun setDummyData(key: String, value: String) {
        if (userData.first().isTestUser) {
            context.filesDir.resolve("$key.json").writeText(value)
        }
    }

    @OptIn(ExperimentalResourceApi::class)
    override suspend fun getDummyData(key: String): String? {
        if (!userData.first().isTestUser) return null
        return suspendRunCatching { Res.readBytes("files/$key.json").decodeToString() }.getOrNull()
    }
}
