package me.matsumo.fanbox.feature.post.image

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.postImageEntry(navigateTo: (Destination) -> Unit, terminate: () -> Unit) {
    entry<Destination.PostImage> { destination ->
        PostImageRoute(
            modifier = Modifier.fillMaxSize(),
            postId = destination.postId,
            postImageIndex = destination.index,
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
