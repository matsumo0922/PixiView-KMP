package me.matsumo.fanbox.feature.about.versions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.intl.Locale
import androidx.compose.ui.unit.dp
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import me.matsumo.fanbox.core.model.Version
import me.matsumo.fanbox.core.model.entity.VersionEntity
import me.matsumo.fanbox.core.resources.Res
import me.matsumo.fanbox.core.resources.about_support_version_history
import me.matsumo.fanbox.core.ui.extensition.Platform
import me.matsumo.fanbox.core.ui.extensition.currentPlatform
import me.matsumo.fanbox.core.ui.theme.end
import me.matsumo.fanbox.core.ui.theme.start
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun VersionHistoryDialog(
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTopAppBarState()
    val versions = getVersions()
    val behavior = TopAppBarDefaults.pinnedScrollBehavior(state)

    Scaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            CenterAlignedTopAppBar(
                modifier = Modifier.fillMaxWidth(),
                title = {
                    Text(
                        text = stringResource(Res.string.about_support_version_history),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                },
                navigationIcon = {
                    if (currentPlatform != Platform.Android) {
                        IconButton(onClick = terminate) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = null,
                            )
                        }
                    }
                },
                scrollBehavior = behavior,
            )
        },
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = paddingValues,
        ) {
            items(
                items = versions,
                key = { item -> item.code },
            ) {
                VersionItem(
                    modifier = Modifier.fillMaxWidth(),
                    version = it,
                )
            }
        }
    }
}

@Composable
private fun VersionItem(
    version: Version,
    modifier: Modifier = Modifier,
) {
    Column(modifier) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    modifier = Modifier.weight(1f),
                    text = "${version.name} [${version.code}]",
                    style = MaterialTheme.typography.bodyMedium.start(),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    modifier = Modifier.weight(1f),
                    text = version.date,
                    style = MaterialTheme.typography.bodyMedium.end(),
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }

            Text(
                modifier = Modifier.fillMaxWidth(),
                text = version.message,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        HorizontalDivider()
    }
}

@OptIn(ExperimentalResourceApi::class)
@Composable
private fun getVersions(): List<Version> {
    val serializer = ListSerializer(VersionEntity.serializer())
    var json by remember { mutableStateOf("") }

    LaunchedEffect(true) {
        json = Res.readBytes("files/versions.json").decodeToString()
    }

    return if (json.isBlank()) {
        emptyList()
    } else {
        Json.decodeFromString(serializer, json)
            .map {
                Version(
                    name = it.versionName,
                    code = it.versionCode,
                    date = it.date,
                    message = if (Locale.current.language == "ja") it.logJp else it.logEn,
                )
            }
            .sortedByDescending { it.code }
    }
}
