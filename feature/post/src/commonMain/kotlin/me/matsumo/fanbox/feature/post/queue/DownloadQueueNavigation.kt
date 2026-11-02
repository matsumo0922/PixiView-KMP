package me.matsumo.fanbox.feature.post.queue

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.downloadQueueEntry(navigateTo: (Destination) -> Unit, terminate: () -> Unit) {
    entry<Destination.DownloadQueue> {
        DownloadQueueScreen(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
