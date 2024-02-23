package me.matsumo.fanbox.feature.setting.oss

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import com.mikepenz.aboutlibraries.ui.compose.LibrariesContainer
import com.mikepenz.aboutlibraries.ui.compose.LibraryDefaults
import dev.icerock.moko.resources.compose.readTextAsState
import dev.icerock.moko.resources.compose.stringResource
import io.github.alexzhirkevich.cupertino.adaptive.AdaptiveScaffold
import io.github.alexzhirkevich.cupertino.adaptive.ExperimentalAdaptiveApi
import me.matsumo.fanbox.core.ui.MR
import me.matsumo.fanbox.feature.setting.SettingTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAdaptiveApi::class)
@Composable
internal fun SettingLicenseScreen(
    terminate: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state = rememberTopAppBarState()
    val behavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(state)
    val libs by MR.files.aboutlibraries.readTextAsState()

    AdaptiveScaffold(
        modifier = modifier.nestedScroll(behavior.nestedScrollConnection),
        topBar = {
            SettingTheme {
                LargeTopAppBar(
                    modifier = Modifier.fillMaxWidth(),
                    title = {
                        Text(
                            text = stringResource(MR.strings.setting_top_others_open_source_license),
                        )
                    },
                    navigationIcon = {
                        Icon(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .padding(6.dp)
                                .clickable { terminate.invoke() },
                            imageVector = Icons.Outlined.ArrowBack,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    },
                    scrollBehavior = behavior,
                )
            }
        },
    ) { paddingValues ->

        // ./gradlew exportLibraryDefinitions -PaboutLibraries.exportPath=../core/ui/src/commonMain/resources/MR/files
        LibrariesContainer(
            modifier = Modifier.fillMaxSize(),
            aboutLibsJson = libs.orEmpty(),
            contentPadding = paddingValues,
            colors = LibraryDefaults.libraryColors(
                backgroundColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                badgeBackgroundColor = MaterialTheme.colorScheme.primary,
                badgeContentColor = MaterialTheme.colorScheme.onPrimary,
                dialogConfirmButtonColor = MaterialTheme.colorScheme.primary,
            ),
        )
    }
}
