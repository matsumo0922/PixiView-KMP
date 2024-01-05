package me.matsumo.fanbox.core.model.pixiv

import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import me.matsumo.fanbox.core.common.serializer.LocalDateTimeSerializer

@Serializable
data class PixivUserAccount(
    @SerialName("access_token")
    val accessToken: String = "",
    @SerialName("expires_in")
    val expiresIn: Int = 0,
    @SerialName("refresh_token")
    val refreshToken: String = "",
    val scope: String = "",
    @SerialName("token_type")
    val tokenType: String = "",
    val user: PixivLocalUser = PixivLocalUser(),
    @SerialName("acquisition_time")
    @Serializable(with = LocalDateTimeSerializer::class)
    val acquisitionTime: LocalDateTime,
)
