package me.matsumo.fanbox.feature.library

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import kotlinx.coroutines.flow.Flow
import me.matsumo.fanbox.core.model.Destination

/**
 * @param onHomeVisibilityChange Library 内でホームのタブを表示しているかが変わったときに呼ぶ。
 * @param openMessageTabRequests 「メッセージ」タブを開く要求。要求を受け取るたびに、「メッセージ」タブを選ぶ。
 */
fun EntryProviderScope<Destination>.libraryEntry(
    navigateTo: (Destination) -> Unit,
    onHomeVisibilityChange: (Boolean) -> Unit,
    openMessageTabRequests: Flow<Unit>,
) {
    entry<Destination.Library> {
        LibraryScreen(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            onHomeVisibilityChange = onHomeVisibilityChange,
            openMessageTabRequests = openMessageTabRequests,
        )
    }
}
