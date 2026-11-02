package me.matsumo.fanbox.feature.post.bookmark

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.bookmarkedPostsEntry(navigateTo: (Destination) -> Unit, terminate: () -> Unit) {
    entry<Destination.BookmarkedPosts> {
        BookmarkedPostsRoute(
            modifier = Modifier.fillMaxSize(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
