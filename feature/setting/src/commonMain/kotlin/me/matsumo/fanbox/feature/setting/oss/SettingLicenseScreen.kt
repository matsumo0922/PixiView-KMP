package me.matsumo.fanbox.feature.setting.oss

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.entity.Library
import com.mikepenz.aboutlibraries.ui.compose.rememberLibraries
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveScaffold
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import me.matsumo.fanbox.core.ui.Res
import me.matsumo.fanbox.core.ui.setting_top_others_open_source_license
import me.matsumo.fanbox.feature.setting.SettingTheme
import me.matsumo.fanbox.feature.setting.oss.components.LibraryItem
import me.matsumo.fanbox.feature.setting.oss.components.LicenseDialog
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.stringResource

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAdaptiveApi::class, ExperimentalResourceApi::class)
@Composable
internal fun SettingLicenseScreen(
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
    val libs by rememberLibraries {
        Res.readBytes("files/aboutlibraries.json").decodeToString()
    }

    val selectedLibrary = remember { mutableStateOf<Library?>(null) }

    AdaptiveScaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            SettingTheme {
                LargeTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(
                            text = stringResource(Res.string.setting_top_others_open_source_license),
                        )
                    },
                    navigationIcon = {
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .padding(6.dp)
                                .clickable { terminate.invoke() },
                            imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    scrollBehavior = behavior,
                )
            }
        },
    ) { paddingValues ->
        // ./gradlew exportLibraryDefinitions -PexportPath="../core/ui/src/commonMain/composeResources/files" (for Windows)
        // ./gradlew exportLibraryDefinitions -PaboutLibraries.exportPath=../core/ui/src/commonMain/composeResources/files (for Mac)

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                top = 16.dp + paddingValues.calculateTopPadding(),
                bottom = 16.dp + paddingValues.calculateBottomPadding(),
                start = 16.dp,
                end = 16.dp,
            ),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            items(libs?.libraries.orEmpty()) { library ->
                LibraryItem(
                    modifier = Modifier.fillMaxWidth(),
                    library = library,
                    onClick = { selectedLibrary.value = it },
                )
            }
        }
    }

    val library = selectedLibrary.value

    if (library != null) {
        LicenseDialog(
            library = library,
            onDismissRequest = { selectedLibrary.value = null },
        )
    }
}
