package me.matsumo.fanbox.core.model.fanbox

import androidx.compose.runtime.Immutable

/**
 * ページング用のカーソル。
 *
 * FANBOX API の `nextUrl` などに含まれるクエリパラメータを保持し、次ページ取得時のリクエストに用いる。
 */
@Immutable
data class Cursor(
    val firstPublishedDatetime: String?,
    val maxPublishedDatetime: String?,
    val firstId: String?,
    val maxId: String?,
    val limit: Int?,
)
