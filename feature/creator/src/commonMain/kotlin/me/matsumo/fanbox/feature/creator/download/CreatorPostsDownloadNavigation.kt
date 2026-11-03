package me.matsumo.fanbox.feature.creator.download

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.creatorPostsDownloadEntry(navigateTo: (Destination) -> Unit, terminate: () -> Unit) {
    entry<Destination.CreatorPostsDownload> { destination ->
        CreatorPostsDownloadRoute(
            modifier = Modifier.fillMaxSize(),
            destination = destination,
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
