package me.matsumo.fanbox.core.model.fanbox

import kotlin.jvm.JvmInline

/** 投稿内のアイテム（画像・動画・ファイル）を一意に識別する ID。 */
@JvmInline
value class PostItemId(val value: String)
