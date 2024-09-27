package me.matsumo.fanbox.core.model.fanbox.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxPaidRecordEntity(
    @SerialName("body")
    val body: List<Body>,
) {
    @Serializable
    data class Body(
        @SerialName("creator")
        val creator: Creator,
        @SerialName("id")
        val id: String,
        @SerialName("paidAmount")
        val paidAmount: Int,
        @SerialName("paymentDatetime")
        val paymentDatetime: String,
        @SerialName("paymentMethod")
        val paymentMethod: String,
    ) {
        @Serializable
        data class Creator(
            @SerialName("creatorId")
            val creatorId: String?,
            @SerialName("isActive")
            val isActive: Boolean,
            @SerialName("user")
            val user: User?,
        ) {
            @Serializable
            data class User(
                @SerialName("iconUrl")
                val iconUrl: String?,
                @SerialName("name")
                val name: String,
                @SerialName("userId")
                val userId: String,
            )
        }
    }
}
