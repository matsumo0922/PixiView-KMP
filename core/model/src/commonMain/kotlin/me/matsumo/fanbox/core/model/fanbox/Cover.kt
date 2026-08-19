package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/** 投稿のカバー画像。 */
@Immutable
data class Cover(
    val url: String,
    val type: String,
)
