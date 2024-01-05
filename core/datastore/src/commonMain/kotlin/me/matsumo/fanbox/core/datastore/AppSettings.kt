package me.matsumo.fanbox.core.datastore

import io.github.xxfast.kstore.KStore
import me.matsumo.fanbox.core.model.UserData

expect val appSettings: KStore<UserData>
