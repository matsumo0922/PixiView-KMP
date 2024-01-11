package me.matsumo.fanbox.feature.setting.theme.items

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.ThemeColorConfig
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.core.ui.theme.DarkDefaultColorScheme
import me.matsumo.fanbox.core.ui.theme.LightDefaultColorScheme
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
import me.matsumo.fanbox.feature.setting.SettingTextItem

@Composable
internal fun SettingThemeColorSection(
    isUseDynamicColor: Boolean,
    themeConfig: ThemeConfig,
    themeColorConfig: ThemeColorConfig,
    onSelectThemeColor: (ThemeColorConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    val isDarkMode = when (themeConfig) {
        ThemeConfig.Light -> false
        ThemeConfig.Dark -> true
        else -> isSystemInDarkTheme()
    }

    Column(modifier) {
        SettingTextItem(
            modifier = Modifier.fillMaxWidth(),
            title = MR.strings.setting_theme_color,
            description = MR.strings.setting_theme_color_description,
            onClick = { /* do nothing */ },
            isEnabled = !isUseDynamicColor,
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        ) {
            items(
                items = ThemeColorConfig.entries.toTypedArray(),
                key = { it.name },
            ) {
                val color = when (it) {
                    ThemeColorConfig.Red -> if (isDarkMode) DarkDefaultColorScheme else LightDefaultColorScheme
                    ThemeColorConfig.Blue -> if (isDarkMode) DarkBlueColorScheme else LightBlueColorScheme
                    ThemeColorConfig.Brown -> if (isDarkMode) DarkBrownColorScheme else LightBrownColorScheme
                    ThemeColorConfig.Green -> if (isDarkMode) DarkGreenColorScheme else LightGreenColorScheme
                    ThemeColorConfig.Purple -> if (isDarkMode) DarkPurpleColorScheme else LightPurpleColorScheme
                    ThemeColorConfig.Pink -> if (isDarkMode) DarkPinkColorScheme else LightPinckColorScheme
                }

                SettingThemeColorItem(
                    onClick = { onSelectThemeColor.invoke(it) },
                    isEnabled = !isUseDynamicColor,
                    isSelected = it == themeColorConfig,
                    backgroundColor = color.surfaceVariant,
                    primaryColor = color.primary,
                    secondaryColor = color.secondary,
                    tertiaryColor = color.tertiary,
                )
            }
        }
    }
}

@Composable
private fun SettingThemeColorItem(
    isEnabled: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    backgroundColor: Color,
    primaryColor: Color,
    secondaryColor: Color,
    tertiaryColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .padding(8.dp)
            .size(96.dp)
            .clip(RoundedCornerShape(8.dp))
            .border(
                width = 1.dp,
                color = if (isSelected && isEnabled) tertiaryColor else Color.Transparent,
                shape = RoundedCornerShape(8.dp),
            )
            .background(backgroundColor.copy(alpha = if (isSelected && isEnabled) 1f else 0.5f))
            .clickable(
                enabled = isEnabled,
                onClick = onClick,
            ),
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(16.dp)
                .clip(CircleShape),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(primaryColor.copy(alpha = if (isEnabled) 1f else 0.5f)),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .background(secondaryColor.copy(alpha = if (isEnabled) 1f else 0.5f)),
            )
        }
    }
}
