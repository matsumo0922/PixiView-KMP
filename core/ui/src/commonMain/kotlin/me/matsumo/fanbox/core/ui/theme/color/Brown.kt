@file:Suppress("UnusedPrivateMember")

package me.matsumo.fanbox.core.ui.theme.color

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

private val md_theme_light_primary = Color(0xFF7B5900)
private val md_theme_light_onPrimary = Color(0xFFFFFFFF)
private val md_theme_light_primaryContainer = Color(0xFFFFDEA4)
private val md_theme_light_onPrimaryContainer = Color(0xFF261900)
private val md_theme_light_secondary = Color(0xFF6C5C3F)
private val md_theme_light_onSecondary = Color(0xFFFFFFFF)
private val md_theme_light_secondaryContainer = Color(0xFFF6E0BB)
private val md_theme_light_onSecondaryContainer = Color(0xFF251A04)
private val md_theme_light_tertiary = Color(0xFF4C6545)
private val md_theme_light_onTertiary = Color(0xFFFFFFFF)
private val md_theme_light_tertiaryContainer = Color(0xFFCDEBC2)
private val md_theme_light_onTertiaryContainer = Color(0xFF092007)
private val md_theme_light_error = Color(0xFFBA1A1A)
private val md_theme_light_errorContainer = Color(0xFFFFDAD6)
private val md_theme_light_onError = Color(0xFFFFFFFF)
private val md_theme_light_onErrorContainer = Color(0xFF410002)
private val md_theme_light_background = Color(0xFFFFFBFF)
private val md_theme_light_onBackground = Color(0xFF1E1B16)
private val md_theme_light_surface = Color(0xFFFFFBFF)
private val md_theme_light_onSurface = Color(0xFF1E1B16)
private val md_theme_light_surfaceVariant = Color(0xFFEEE1CF)
private val md_theme_light_onSurfaceVariant = Color(0xFF4E4639)
private val md_theme_light_outline = Color(0xFF7F7667)
private val md_theme_light_inverseOnSurface = Color(0xFFF8EFE7)
private val md_theme_light_inverseSurface = Color(0xFF34302A)
private val md_theme_light_inversePrimary = Color(0xFFF5BE48)
private val md_theme_light_shadow = Color(0xFF000000)
private val md_theme_light_surfaceTint = Color(0xFF7B5900)
private val md_theme_light_outlineVariant = Color(0xFFD1C5B4)
private val md_theme_light_scrim = Color(0xFF000000)

private val md_theme_dark_primary = Color(0xFFF5BE48)
private val md_theme_dark_onPrimary = Color(0xFF412D00)
private val md_theme_dark_primaryContainer = Color(0xFF5D4200)
private val md_theme_dark_onPrimaryContainer = Color(0xFFFFDEA4)
private val md_theme_dark_secondary = Color(0xFFD9C4A0)
private val md_theme_dark_onSecondary = Color(0xFF3B2F15)
private val md_theme_dark_secondaryContainer = Color(0xFF53452A)
private val md_theme_dark_onSecondaryContainer = Color(0xFFF6E0BB)
private val md_theme_dark_tertiary = Color(0xFFB2CFA8)
private val md_theme_dark_onTertiary = Color(0xFF1F361A)
private val md_theme_dark_tertiaryContainer = Color(0xFF354D2F)
private val md_theme_dark_onTertiaryContainer = Color(0xFFCDEBC2)
private val md_theme_dark_error = Color(0xFFFFB4AB)
private val md_theme_dark_errorContainer = Color(0xFF93000A)
private val md_theme_dark_onError = Color(0xFF690005)
private val md_theme_dark_onErrorContainer = Color(0xFFFFDAD6)
private val md_theme_dark_background = Color(0xFF1E1B16)
private val md_theme_dark_onBackground = Color(0xFFE9E1D9)
private val md_theme_dark_surface = Color(0xFF1E1B16)
private val md_theme_dark_onSurface = Color(0xFFE9E1D9)
private val md_theme_dark_surfaceVariant = Color(0xFF4E4639)
private val md_theme_dark_onSurfaceVariant = Color(0xFFD1C5B4)
private val md_theme_dark_outline = Color(0xFF9A8F80)
private val md_theme_dark_inverseOnSurface = Color(0xFF1E1B16)
private val md_theme_dark_inverseSurface = Color(0xFFE9E1D9)
private val md_theme_dark_inversePrimary = Color(0xFF7B5900)
private val md_theme_dark_shadow = Color(0xFF000000)
private val md_theme_dark_surfaceTint = Color(0xFFF5BE48)
private val md_theme_dark_outlineVariant = Color(0xFF4E4639)
private val md_theme_dark_scrim = Color(0xFF000000)

val LightBrownColorScheme = lightColorScheme(
    primary = md_theme_light_primary,
    onPrimary = md_theme_light_onPrimary,
    primaryContainer = md_theme_light_primaryContainer,
    onPrimaryContainer = md_theme_light_onPrimaryContainer,
    secondary = md_theme_light_secondary,
    onSecondary = md_theme_light_onSecondary,
    secondaryContainer = md_theme_light_secondaryContainer,
    onSecondaryContainer = md_theme_light_onSecondaryContainer,
    tertiary = md_theme_light_tertiary,
    onTertiary = md_theme_light_onTertiary,
    tertiaryContainer = md_theme_light_tertiaryContainer,
    onTertiaryContainer = md_theme_light_onTertiaryContainer,
    error = md_theme_light_error,
    errorContainer = md_theme_light_errorContainer,
    onError = md_theme_light_onError,
    onErrorContainer = md_theme_light_onErrorContainer,
    background = md_theme_light_background,
    onBackground = md_theme_light_onBackground,
    surface = md_theme_light_surface,
    onSurface = md_theme_light_onSurface,
    surfaceVariant = md_theme_light_surfaceVariant,
    onSurfaceVariant = md_theme_light_onSurfaceVariant,
    outline = md_theme_light_outline,
    inverseOnSurface = md_theme_light_inverseOnSurface,
    inverseSurface = md_theme_light_inverseSurface,
    inversePrimary = md_theme_light_inversePrimary,
    surfaceTint = md_theme_light_surfaceTint,
    outlineVariant = md_theme_light_outlineVariant,
    scrim = md_theme_light_scrim,
)

val DarkBrownColorScheme = darkColorScheme(
    primary = md_theme_dark_primary,
    onPrimary = md_theme_dark_onPrimary,
    primaryContainer = md_theme_dark_primaryContainer,
    onPrimaryContainer = md_theme_dark_onPrimaryContainer,
    secondary = md_theme_dark_secondary,
    onSecondary = md_theme_dark_onSecondary,
    secondaryContainer = md_theme_dark_secondaryContainer,
    onSecondaryContainer = md_theme_dark_onSecondaryContainer,
    tertiary = md_theme_dark_tertiary,
    onTertiary = md_theme_dark_onTertiary,
    tertiaryContainer = md_theme_dark_tertiaryContainer,
    onTertiaryContainer = md_theme_dark_onTertiaryContainer,
    error = md_theme_dark_error,
    errorContainer = md_theme_dark_errorContainer,
    onError = md_theme_dark_onError,
    onErrorContainer = md_theme_dark_onErrorContainer,
    background = md_theme_dark_background,
    onBackground = md_theme_dark_onBackground,
    surface = md_theme_dark_surface,
    onSurface = md_theme_dark_onSurface,
    surfaceVariant = md_theme_dark_surfaceVariant,
    onSurfaceVariant = md_theme_dark_onSurfaceVariant,
    outline = md_theme_dark_outline,
    inverseOnSurface = md_theme_dark_inverseOnSurface,
    inverseSurface = md_theme_dark_inverseSurface,
    inversePrimary = md_theme_dark_inversePrimary,
    surfaceTint = md_theme_dark_surfaceTint,
    outlineVariant = md_theme_dark_outlineVariant,
    scrim = md_theme_dark_scrim,
)
