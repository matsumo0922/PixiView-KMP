package me.matsumo.fanbox.feature.post.search.creator

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.postByCreatorSearchEntry(navigateTo: (Destination) -> Unit, terminate: () -> Unit) {
    entry<Destination.PostByCreatorSearch> { destination ->
        PostByCreatorSearchRoute(
            modifier = Modifier.fillMaxSize(),
            destination = destination,
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
