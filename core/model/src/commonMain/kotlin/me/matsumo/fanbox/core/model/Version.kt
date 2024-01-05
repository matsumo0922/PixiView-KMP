package me.matsumo.fanbox.core.model

import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.common.serializer.LocalDateSerializer

@Serializable
data class Version(
    val name: String,
    val code: Int,
    val message: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
)
