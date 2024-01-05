package me.matsumo.fanbox.core.model.fanbox.entity

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FanboxPostCommentItemsEntity(
    @SerialName("body")
    val body: FanboxPostDetailEntity.Body.CommentList,
)
