package me.matsumo.fanbox.feature.about.about

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.aboutEntry(navigateTo: (Destination) -> Unit, terminate: () -> Unit) {
    entry<Destination.About> {
        AboutRoute(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
