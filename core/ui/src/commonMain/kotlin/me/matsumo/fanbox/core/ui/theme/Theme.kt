package me.matsumo.fanbox.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveTheme
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import io.github.alexzhirkevich.cupertino.adaptive.Theme
import io.github.alexzhirkevich.cupertino.theme.CupertinoTheme
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.model.fanbox.FanboxMetaData
import me.matsumo.fanbox.core.ui.extensition.FanboxCookie
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxCookie
import me.matsumo.fanbox.core.ui.extensition.LocalFanboxMetadata
import me.matsumo.fanbox.core.ui.theme.color.DarkBlueColorScheme
import me.matsumo.fanbox.core.ui.theme.color.DarkBrownColorScheme
import me.matsumo.fanbox.core.ui.theme.color.DarkGreenColorScheme
import me.matsumo.fanbox.core.ui.theme.color.DarkPinkColorScheme
import me.matsumo.fanbox.core.ui.theme.color.DarkPurpleColorScheme
import me.matsumo.fanbox.core.ui.theme.color.LightBlueColorScheme
import me.matsumo.fanbox.core.ui.theme.color.LightBrownColorScheme
import me.matsumo.fanbox.core.ui.theme.color.LightGreenColorScheme
import me.matsumo.fanbox.core.ui.theme.color.LightPinckColorScheme
import me.matsumo.fanbox.core.ui.theme.color.LightPurpleColorScheme
import me.matsumo.fanbox.core.ui.view.LocalNativeViewsProvider
import me.matsumo.fanbox.core.ui.view.NativeView
import me.matsumo.fanbox.core.ui.view.NativeViews

typealias ColorSchemeIOS = io.github.alexzhirkevich.cupertino.theme.ColorScheme

expect fun determineTheme(): Theme

val LightDefaultColorScheme = lightColorScheme(
    primary = Purple40,
    onPrimary = Color.White,
    primaryContainer = Purple90,
    onPrimaryContainer = Purple10,
    secondary = Orange40,
    onSecondary = Color.White,
    secondaryContainer = Orange90,
    onSecondaryContainer = Orange10,
    tertiary = Blue40,
    onTertiary = Color.White,
    tertiaryContainer = Blue90,
    onTertiaryContainer = Blue10,
    error = Red40,
    onError = Color.White,
    errorContainer = Red90,
    onErrorContainer = Red10,
    background = DarkPurpleGray99,
    onBackground = DarkPurpleGray10,
    surface = DarkPurpleGray99,
    onSurface = DarkPurpleGray10,
    surfaceVariant = PurpleGray90,
    onSurfaceVariant = PurpleGray30,
    inverseSurface = DarkPurpleGray20,
    inverseOnSurface = DarkPurpleGray95,
    outline = PurpleGray50,
)

val DarkDefaultColorScheme = darkColorScheme(
    primary = Purple80,
    onPrimary = Purple20,
    primaryContainer = Purple30,
    onPrimaryContainer = Purple90,
    secondary = Orange80,
    onSecondary = Orange20,
    secondaryContainer = Orange30,
    onSecondaryContainer = Orange90,
    tertiary = Blue80,
    onTertiary = Blue20,
    tertiaryContainer = Blue30,
    onTertiaryContainer = Blue90,
    error = Red80,
    onError = Red20,
    errorContainer = Red30,
    onErrorContainer = Red90,
    background = DarkPurpleGray10,
    onBackground = DarkPurpleGray90,
    surface = DarkPurpleGray10,
    onSurface = DarkPurpleGray90,
    surfaceVariant = PurpleGray30,
    onSurfaceVariant = PurpleGray80,
    inverseSurface = DarkPurpleGray90,
    inverseOnSurface = DarkPurpleGray10,
    outline = PurpleGray60,
)

val LocalColorScheme = staticCompositionLocalOf { LightDefaultColorScheme }

@OptIn(ExperimentalAdaptiveApi::class)
@Composable
fun PixiViewTheme(
    fanboxCookie: String = "",
    fanboxMetadata: FanboxMetaData = FanboxMetaData.dummy(),
    pixiViewConfig: PixiViewConfig = PixiViewConfig.dummy(),
    themeConfig: ThemeConfig = ThemeConfig.System,
    themeColorConfig: ThemeColorConfig = ThemeColorConfig.Red,
    nativeViews: Map<String, () -> NativeView?> = emptyMap(),
    content: @Composable () -> Unit,
) {
    val shouldUseDarkTheme = shouldUseDarkTheme(themeConfig)

    val colorScheme = when (themeColorConfig) {
        ThemeColorConfig.Blue -> if (shouldUseDarkTheme) DarkBlueColorScheme else LightBlueColorScheme
        ThemeColorConfig.Brown -> if (shouldUseDarkTheme) DarkBrownColorScheme else LightBrownColorScheme
        ThemeColorConfig.Green -> if (shouldUseDarkTheme) DarkGreenColorScheme else LightGreenColorScheme
        ThemeColorConfig.Purple -> if (shouldUseDarkTheme) DarkPurpleColorScheme else LightPurpleColorScheme
        ThemeColorConfig.Pink -> if (shouldUseDarkTheme) DarkPinkColorScheme else LightPinckColorScheme
        else -> if (shouldUseDarkTheme) DarkDefaultColorScheme else LightDefaultColorScheme
    }

    val backgroundTheme = BackgroundTheme(
        color = colorScheme.surface,
        tonalElevation = 2.dp,
    )

    val tintTheme = TintTheme()

    CompositionLocalProvider(
        LocalPixiViewConfig provides pixiViewConfig,
        LocalColorScheme provides colorScheme,
        LocalFanboxCookie provides FanboxCookie(fanboxCookie),
        LocalFanboxMetadata provides fanboxMetadata,
        LocalBackgroundTheme provides backgroundTheme,
        LocalTintTheme provides tintTheme,
        LocalNativeViewsProvider provides NativeViews(nativeViews),
        LocalShouldUseDarkTheme provides shouldUseDarkTheme,
    ) {
        AdaptiveTheme(
            material = {
                MaterialTheme(
                    colorScheme = colorScheme,
                    typography = PixiViewTypography,
                    content = it,
                )
            },
            cupertino = {
                CupertinoTheme(
                    colorScheme = convertMaterialToIOSColorScheme(colorScheme, shouldUseDarkTheme),
                    content = it
                )
            },
            target = determineTheme()
        ) {
            content.invoke()
        }
    }
}

fun convertMaterialToIOSColorScheme(materialScheme: ColorScheme, isDark: Boolean): ColorSchemeIOS {
    return if (isDark) {
        io.github.alexzhirkevich.cupertino.theme.darkColorScheme(
            accent = materialScheme.secondary,
            label = materialScheme.onSurface,
            secondaryLabel = materialScheme.onSurfaceVariant,
            tertiaryLabel = materialScheme.onBackground,
            quaternaryLabel = materialScheme.onPrimaryContainer,
            systemFill = materialScheme.primary,
            secondarySystemFill = materialScheme.primaryContainer,
            tertiarySystemFill = materialScheme.tertiary,
            quaternarySystemFill = materialScheme.tertiaryContainer,
            placeholderText = materialScheme.onBackground,
            separator = materialScheme.outline,
            opaqueSeparator = materialScheme.outlineVariant,
            link = materialScheme.secondary,
            systemGroupedBackground = materialScheme.surface,
            secondarySystemGroupedBackground = materialScheme.surfaceVariant,
            tertiarySystemGroupedBackground = materialScheme.background,
            systemBackground = materialScheme.surface,
            secondarySystemBackground = materialScheme.surfaceVariant,
            tertiarySystemBackground = materialScheme.background,
        )
    } else {
        io.github.alexzhirkevich.cupertino.theme.lightColorScheme(
            accent = materialScheme.secondary,
            label = materialScheme.onSurface,
            secondaryLabel = materialScheme.onSurfaceVariant,
            tertiaryLabel = materialScheme.onBackground,
            quaternaryLabel = materialScheme.onPrimaryContainer,
            systemFill = materialScheme.primary,
            secondarySystemFill = materialScheme.primaryContainer,
            tertiarySystemFill = materialScheme.tertiary,
            quaternarySystemFill = materialScheme.tertiaryContainer,
            placeholderText = materialScheme.onBackground,
            separator = materialScheme.outline,
            opaqueSeparator = materialScheme.outlineVariant,
            link = materialScheme.secondary,
            systemGroupedBackground = materialScheme.surface,
            secondarySystemGroupedBackground = materialScheme.surfaceVariant,
            tertiarySystemGroupedBackground = materialScheme.background,
            systemBackground = materialScheme.surface,
            secondarySystemBackground = materialScheme.surfaceVariant,
            tertiarySystemBackground = materialScheme.background,
        )
    }
}

@Composable
fun shouldUseDarkTheme(themeConfig: ThemeConfig): Boolean {
    return when (themeConfig) {
        ThemeConfig.System -> isSystemInDarkTheme()
        ThemeConfig.Light -> false
        ThemeConfig.Dark -> true
    }
}

val LocalShouldUseDarkTheme = staticCompositionLocalOf { false }