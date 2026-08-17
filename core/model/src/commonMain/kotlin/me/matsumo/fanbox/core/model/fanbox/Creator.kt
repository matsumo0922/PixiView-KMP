package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/** クリエイター。ユーザー情報と紐づく。 */
@Immutable
data class Creator(
    val creatorId: CreatorId?,
    val user: User?,
)
