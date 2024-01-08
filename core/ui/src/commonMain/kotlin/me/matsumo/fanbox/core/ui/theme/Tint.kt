@file:Suppress("MatchingDeclarationName")

package me.matsumo.fanbox.core.ui.theme

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class TintTheme(
    val iconTint: Color? = null,
)

val LocalTintTheme = staticCompositionLocalOf { TintTheme() }
