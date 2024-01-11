package me.matsumo.fanbox.feature.setting.theme.items

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.model.ThemeConfig
import me.matsumo.fanbox.core.ui.theme.bold

@Composable
internal fun SettingThemeTabsSection(
    themeConfig: ThemeConfig,
    onSelectTheme: (ThemeConfig) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier) {
        Row(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.onSurface)
                .padding(4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            TabText(
                text = "Light",
                isSelected = themeConfig == ThemeConfig.Light,
                onClick = { onSelectTheme.invoke(ThemeConfig.Light) },
            )

            TabText(
                text = "Dark",
                isSelected = themeConfig == ThemeConfig.Dark,
                onClick = { onSelectTheme.invoke(ThemeConfig.Dark) },
            )

            TabText(
                text = "System",
                isSelected = themeConfig == ThemeConfig.System,
                onClick = { onSelectTheme.invoke(ThemeConfig.System) },
            )
        }
    }
}

@Composable
private fun TabText(
    text: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier
            .clip(CircleShape)
            .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
            .clickable { onClick.invoke() }
            .padding(horizontal = 24.dp, vertical = 8.dp),
        text = text,
        style = MaterialTheme.typography.bodyMedium.bold(),
        color = if (isSelected) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.surfaceVariant,
    )
}
