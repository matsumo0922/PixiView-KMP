package me.matsumo.fanbox.feature.about.about.items

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource

@Composable
internal fun AboutThanksItem(
    titleRes: StringResource,
    descriptionRes: StringResource,
    iconVector: ImageVector,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit,
) {
    Row(
        modifier = modifier.padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalArrangement = Arrangement.spacedBy(18.dp),
    ) {
        Icon(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape),
            imageVector = iconVector,
            tint = MaterialTheme.colorScheme.onSurface,
            contentDescription = null,
        )

        AboutDescriptionItem(
            modifier = Modifier.weight(1f),
            titleRes = titleRes,
            descriptionRes = descriptionRes,
            content = content,
        )
    }
}
