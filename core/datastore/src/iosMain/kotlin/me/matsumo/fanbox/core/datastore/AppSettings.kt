package me.matsumo.fanbox.core.datastore

import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import me.matsumo.fanbox.core.model.UserData
import okio.Path.Companion.toPath

actual val appSettings: KStore<UserData> by lazy {
    storeOf("${appFileDir}/pixiview-settings.json".toPath(), UserData.dummy())
}
