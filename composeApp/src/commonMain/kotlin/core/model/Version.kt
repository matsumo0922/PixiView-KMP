package core.model

import core.common.serializer.LocalDateSerializer
import kotlinx.datetime.LocalDate
import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val name: String,
    val code: Int,
    val message: String,
    @Serializable(with = LocalDateSerializer::class)
    val date: LocalDate,
)
