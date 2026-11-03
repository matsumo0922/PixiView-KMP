package me.matsumo.fanbox.feature.creator.fancard

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.fanCardEntry(terminate: () -> Unit) {
    entry<Destination.FanCard> { destination ->
        FanCardRoute(
            modifier = Modifier.fillMaxSize(),
            destination = destination,
            terminate = terminate,
        )
    }
}
