package me.matsumo.fanbox.core.repository

import me.matsumo.fanbox.core.datastore.LaunchLogDataStore

class LaunchLogRepository(
    private val launchLogDataStore: LaunchLogDataStore,
) {
    fun launch() {
        launchLogDataStore.launch()
    }

    suspend fun getLaunchCount(): Int {
        return launchLogDataStore.getLaunchCount()
    }
}
