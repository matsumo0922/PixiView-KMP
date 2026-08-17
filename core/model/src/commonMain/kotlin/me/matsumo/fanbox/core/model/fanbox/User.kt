package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/** FANBOX のユーザー情報。 */
@Immutable
data class User(
    val userId: UserId?,
    val creatorId: CreatorId?,
    val name: String,
    val iconUrl: String?,
)
