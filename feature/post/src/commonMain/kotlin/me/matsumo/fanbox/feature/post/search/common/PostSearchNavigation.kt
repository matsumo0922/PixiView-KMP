package me.matsumo.fanbox.feature.post.search.common

import androidx.navigation3.runtime.EntryProviderScope
import me.matsumo.fanbox.core.model.Destination

fun EntryProviderScope<Destination>.postSearchEntry(navigateTo: (Destination) -> Unit, terminate: () -> Unit) {
    entry<Destination.PostSearch> { destination ->
        val query = buildQuery(destination.creatorId, destination.creatorQuery, destination.tag)

        PostSearchRoute(
            query = query.takeIf { parseQuery(it).mode != PostSearchMode.Unknown }.orEmpty(),
            navigateTo = navigateTo,
            terminate = terminate,
        )
    }
}
