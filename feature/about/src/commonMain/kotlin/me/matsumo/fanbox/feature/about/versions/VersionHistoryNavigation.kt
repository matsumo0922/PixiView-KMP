package me.matsumo.fanbox.feature.about.versions

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.backhandler.BackHandler
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.ui.navigation.BottomSheetSceneStrategy

@OptIn(ExperimentalComposeUiApi::class)
fun EntryProviderScope<Destination>.versionHistoryBottomSheetEntry(terminate: () -> Unit) {
    entry<Destination.VersionHistoryBottomSheet>(
        metadata = BottomSheetSceneStrategy.bottomSheet(),
    ) {
        BackHandler {
            terminate()
        }

        VersionHistoryDialog(
            modifier = Modifier.fillMaxSize(),
            terminate = terminate,
        )
    }
}
