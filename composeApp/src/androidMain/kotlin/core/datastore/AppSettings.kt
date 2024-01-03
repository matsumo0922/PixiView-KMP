package core.datastore

import core.helper.appFileDir
import core.model.UserData
import io.github.xxfast.kstore.KStore
import io.github.xxfast.kstore.file.storeOf
import okio.Path.Companion.toPath

actual val appSettings: KStore<UserData> by lazy {
    storeOf("${appFileDir}/pixiview-settings.json".toPath(), UserData.dummy())
}
