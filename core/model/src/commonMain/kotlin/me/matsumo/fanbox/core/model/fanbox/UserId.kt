package me.matsumo.fanbox.core.model.fanbox

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** ユーザーを一意に識別する ID。 */
@Serializable
@JvmInline
value class UserId(val value: Long)
