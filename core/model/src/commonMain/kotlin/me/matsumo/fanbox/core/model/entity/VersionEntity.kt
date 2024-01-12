package me.matsumo.fanbox.core.model.entity

import kotlinx.serialization.Serializable

@Serializable
data class VersionEntity(
    val versionName: String,
    val versionCode: Int,
    val date: String,
    val logJp: String,
    val logEn: String,
)
