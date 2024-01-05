package me.matsumo.fanbox.core.model.pixiv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PixivLocalUser(
    val account: String = "",
    val id: String = "",
    @SerialName("is_mail_authorized")
    val isMailAuthorized: Boolean = false,
    @SerialName("is_premium")
    val isPremium: Boolean = false,
    @SerialName("mail_address")
    val mailAddress: String = "",
    val name: String = "",
    @SerialName("profile_image_urls")
    val profileImageUrls: ProfileImageUrls = ProfileImageUrls(),
    @SerialName("require_policy_agreement")
    val requirePolicyAgreement: Boolean = false,
    @SerialName("x_restrict")
    val xRestrict: Int = 0,
) {
    @Serializable
    data class ProfileImageUrls(
        @SerialName("px_16x16")
        val px16x16: String = "",
        @SerialName("px_170x170")
        val px170x170: String = "",
        @SerialName("px_50x50")
        val px50x50: String = "",
    )
}
