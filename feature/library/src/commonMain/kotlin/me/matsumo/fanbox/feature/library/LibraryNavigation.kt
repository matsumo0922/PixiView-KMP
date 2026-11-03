package me.matsumo.fanbox.feature.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

/**
 * @param onHomeVisibilityChange Library 内でホームのタブを表示しているかが変わったときに呼ぶ。
 */
fun EntryProviderScope<Destination>.libraryEntry(
    navigateTo: (Destination) -> Unit,
    onHomeVisibilityChange: (Boolean) -> Unit,
) {
    entry<Destination.Library> {
        LibraryScreen(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            onHomeVisibilityChange = onHomeVisibilityChange,
        )
    }
}
