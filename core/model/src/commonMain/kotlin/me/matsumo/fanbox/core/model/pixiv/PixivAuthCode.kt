package me.matsumo.fanbox.core.model.pixiv

data class PixivAuthCode(
    var code: String,
    val codeVerifier: String,
    val codeChallenge: String,
    val url: String,
)
