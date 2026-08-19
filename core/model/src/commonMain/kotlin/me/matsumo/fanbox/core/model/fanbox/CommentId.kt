package me.matsumo.fanbox.core.model.fanbox

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** コメントを一意に識別する ID。 */
@Serializable
@JvmInline
value class CommentId(val value: String)
