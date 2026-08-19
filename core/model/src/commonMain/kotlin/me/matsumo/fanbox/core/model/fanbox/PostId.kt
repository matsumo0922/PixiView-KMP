package me.matsumo.fanbox.core.model.fanbox

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** 投稿を一意に識別する ID。 */
@Serializable
@JvmInline
value class PostId(val value: String)
