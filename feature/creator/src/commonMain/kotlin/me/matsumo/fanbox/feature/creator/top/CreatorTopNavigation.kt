package me.matsumo.fanbox.feature.creator.top

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents

fun EntryProviderScope<Destination>.creatorTopEntry(
    navigateTo: (Destination) -> Unit,
    navigateToAlertDialog: (SimpleAlertContents, () -> Unit, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    entry<Destination.CreatorTop> { destination ->
        CreatorTopRoute(
            modifier = Modifier.fillMaxSize(),
            destination = destination,
            navigateTo = navigateTo,
            navigateToAlertDialog = navigateToAlertDialog,
            terminate = terminate,
        )
    }
}
