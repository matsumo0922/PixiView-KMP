package me.matsumo.fanbox.core.model.fanbox

import kotlinx.serialization.Serializable
import kotlin.jvm.JvmInline

/** 支援プランを一意に識別する ID。 */
@Serializable
@JvmInline
value class PlanId(val value: String)
