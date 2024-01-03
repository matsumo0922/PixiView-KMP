package core.datastore

import core.model.UserData
import io.github.xxfast.kstore.KStore

expect val appSettings: KStore<UserData>
