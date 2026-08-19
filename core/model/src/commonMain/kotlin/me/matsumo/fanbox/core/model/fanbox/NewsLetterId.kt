package me.matsumo.fanbox.core.model.fanbox

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** ニュースレターを一意に識別する ID。 */
@Serializable
@JvmInline
value class NewsLetterId(val value: String)
