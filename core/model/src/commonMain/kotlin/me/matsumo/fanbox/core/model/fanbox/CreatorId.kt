package me.matsumo.fanbox.core.model.fanbox

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** クリエイターを一意に識別する ID。 */
@Serializable
@JvmInline
value class CreatorId(val value: String)
