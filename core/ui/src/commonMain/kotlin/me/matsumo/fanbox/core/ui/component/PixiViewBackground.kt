package me.matsumo.fanbox.core.ui.component

import androidx.compose.material3.LocalAbsoluteTonalElevation
import androidx.compose.material3.Surface
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import me.matsumo.fanbox.core.ui.theme.LocalBackgroundTheme

@Composable
fun PixiViewBackground(
    modifier: Modifier = Modifier,
    background: Color? = null,
    content: @Composable () -> Unit,
) {
    val localColor = LocalBackgroundTheme.current.color
    val localTotalElevation = LocalBackgroundTheme.current.tonalElevation

    val color = if (localColor == Color.Unspecified) Color.Transparent else localColor
    val totalElevation = if (localTotalElevation == Dp.Unspecified) 0.dp else localTotalElevation

    Surface(
        color = background ?: color,
        contentColor = contentColorFor(color),
        tonalElevation = totalElevation,
        modifier = modifier,
    ) {
        CompositionLocalProvider(LocalAbsoluteTonalElevation provides 0.dp) {
            content()
        }
    }
}
