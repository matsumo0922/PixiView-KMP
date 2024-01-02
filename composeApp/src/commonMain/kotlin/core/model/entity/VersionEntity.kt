package core.model.entity

import core.common.serializer.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class VersionEntity(
    val versionName: String,
    val versionCode: Int,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
    val logJp: String,
    val logEn: String,
)
