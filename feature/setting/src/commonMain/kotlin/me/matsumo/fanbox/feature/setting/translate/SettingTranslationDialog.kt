package me.matsumo.fanbox.feature.setting.translate

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.common_cancel
import me.matsumo.fanbox.core.resources.common_ok
import me.matsumo.fanbox.core.resources.setting_top_theme_translate_language
import me.matsumo.fanbox.core.resources.setting_top_theme_translate_language_description
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
internal fun SettingTranslationDialog(
    defaultLanguage: String,
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: SettingTranslationViewModel = koinViewModel(),
) {
    val scope = rememberCoroutineScope()
    var tag by remember { mutableStateOf(defaultLanguage) }
    var error by remember { mutableStateOf(false) }

    LaunchedEffect(tag) {
        error = false
    }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.setting_top_theme_translate_language),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
        )

        Text(
            modifier = Modifier.fillMaxWidth(),
            text = stringResource(Res.string.setting_top_theme_translate_language_description),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            value = tag,
            onValueChange = { tag = it },
            label = { Text("Language tag") },
            singleLine = true,
            isError = error,
        )

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedButton(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp),
                onClick = { terminate.invoke() },
            ) {
                Text(
                    text = stringResource(Res.string.common_cancel),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                )
            }

            Button(
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(4.dp),
                onClick = {
                    scope.launch {
                        if (viewModel.setTranslateLanguage(tag)) {
                            terminate.invoke()
                        } else {
                            error = true
                        }
                    }
                },
                enabled = tag.isNotBlank() && !error,
            ) {
                Text(
                    text = stringResource(Res.string.common_ok),
                    style = MaterialTheme.typography.labelMedium,
                    color = if (tag.isNotBlank() && !error) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
