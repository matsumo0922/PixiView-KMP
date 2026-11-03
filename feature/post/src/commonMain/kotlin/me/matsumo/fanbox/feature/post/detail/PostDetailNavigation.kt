package me.matsumo.fanbox.feature.post.detail

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination
import me.matsumo.fanbox.core.model.SimpleAlertContents

fun EntryProviderScope<Destination>.postDetailEntry(
    navigateTo: (Destination) -> Unit,
    navigateToCommentDeleteDialog: (SimpleAlertContents, () -> Unit) -> Unit,
    terminate: () -> Unit,
) {
    entry<Destination.PostDetail> { destination ->
        PostDetailRoute(
            modifier = Modifier.fillMaxSize(),
            destination = destination,
            navigateTo = navigateTo,
            navigateToCommentDeleteDialog = navigateToCommentDeleteDialog,
            terminate = terminate,
        )
    }
}
