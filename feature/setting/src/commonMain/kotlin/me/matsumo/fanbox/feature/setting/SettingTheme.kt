package me.matsumo.fanbox.feature.setting

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

@Composable
internal fun SettingTheme(content: @Composable () -> Unit) {
    val typography = MaterialTheme.typography.copy(
        titleLarge = TextStyle(
            fontWeight = FontWeight.Bold,
            fontSize = 18.sp,
            lineHeight = 24.sp,
            letterSpacing = 0.1.sp,
        ),
        headlineMedium = TextStyle(
            fontWeight = FontWeight.Normal,
            fontSize = 32.sp,
            lineHeight = 36.sp,
            letterSpacing = 0.sp,
        ),
    )

    MaterialTheme(
        colorScheme = MaterialTheme.colorScheme,
        typography = typography,
    ) {
        content.invoke()
    }
}
