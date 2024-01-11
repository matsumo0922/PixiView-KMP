package me.matsumo.fanbox.feature.setting.top.items

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import me.matsumo.fanbox.core.ui.theme.bold

@Composable
internal fun SettingTopTitleItem(
    text: StringResource,
    modifier: Modifier = Modifier,
) {
    Text(
        modifier = modifier.padding(
            top = 24.dp,
            bottom = 12.dp,
            start = 24.dp,
            end = 24.dp,
        ),
        text = stringResource(text).uppercase(),
        style = MaterialTheme.typography.bodyMedium.bold(),
        color = MaterialTheme.colorScheme.primary,
    )
}
