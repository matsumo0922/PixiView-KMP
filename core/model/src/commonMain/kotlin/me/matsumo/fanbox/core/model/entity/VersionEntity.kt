package me.matsumo.fanbox.core.model.entity

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.common.serializer.LocalDateSerializer

@Serializable
data class VersionEntity(
    val versionName: String,
    val versionCode: Int,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val logJp: String,
    val logEn: String,
)
