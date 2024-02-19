package me.matsumo.fanbox.core.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.compositeOver
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource

@Composable
fun SettingSwitchItem(
    title: StringResource,
    description: StringResource?,
    value: Boolean,
    onValueChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    isEnabled: Boolean = true,
) {
    val titleColor: Color
    val descriptionColor: Color

    if (isEnabled) {
        titleColor = MaterialTheme.colorScheme.onSurface
        descriptionColor = MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
            .copy(alpha = 0.38f)
            .compositeOver(MaterialTheme.colorScheme.surface)
            .also {
                titleColor = it
                descriptionColor = it
            }
    }

    Row(
        modifier = modifier
            .clickable(
                enabled = isEnabled,
                onClick = { onValueChanged.invoke(!value) },
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(
                space = 4.dp,
                alignment = Alignment.CenterVertically,
            ),
        ) {
            Text(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(title),
                style = MaterialTheme.typography.bodyLarge,
                color = titleColor,
            )

            if (description != null) {
                Text(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(description),
                    style = MaterialTheme.typography.bodyMedium,
                    color = descriptionColor,
                )
            }
        }

        Switch(
            enabled = isEnabled,
            checked = value,
            onCheckedChange = { onValueChanged.invoke(it) },
        )
    }
}
