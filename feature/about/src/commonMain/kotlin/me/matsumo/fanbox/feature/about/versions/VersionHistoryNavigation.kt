package me.matsumo.fanbox.feature.about.versions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation.NavGraphBuilder
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.component.sheet.bottomSheet

@OptIn(ExperimentalComposeUiApi::class)
fun NavGraphBuilder.versionHistoryBottomSheet(
    terminate: () -> Unit,
) {
    bottomSheet<Destination.VersionHistoryBottomSheet> {
        BackHandler {
            terminate()
        }

        VersionHistoryDialog(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
