package me.matsumo.fanbox.feature.about.about.items

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp

@Composable
internal fun AboutIconButton(
    imageVector: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .border(
                width = 0.5.dp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                shape = CircleShape,
            )
            .clickable { onClick.invoke() },
    ) {
        Icon(
            modifier = Modifier
                .padding(7.dp)
                .fillMaxSize(),
            imageVector = imageVector,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
