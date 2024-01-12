package me.matsumo.fanbox.core.model

import kotlinx.serialization.Serializable

@Serializable
data class Version(
    val name: String,
    val code: Int,
    val message: String,
    val date: String,
)
