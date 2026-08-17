package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/** 投稿タグ。 */
@Immutable
data class Tag(
    val count: Int,
    val coverImageUrl: String?,
    val name: String,
)
