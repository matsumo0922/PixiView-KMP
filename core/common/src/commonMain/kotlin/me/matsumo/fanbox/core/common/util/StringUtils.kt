package me.matsumo.fanbox.core.common.util

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDateTime

expect fun Instant.format(pattern: String): String
