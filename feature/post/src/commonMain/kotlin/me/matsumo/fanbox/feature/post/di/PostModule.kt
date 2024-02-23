package me.matsumo.fanbox.feature.post.di

import me.matsumo.fanbox.feature.post.bookmark.BookmarkedPostsViewModel
import me.matsumo.fanbox.feature.post.detail.PostDetailRootViewModel
import me.matsumo.fanbox.feature.post.detail.PostDetailViewModel
import me.matsumo.fanbox.feature.post.image.PostImageViewModel
import me.matsumo.fanbox.feature.post.search.PostSearchViewModel
import org.koin.dsl.module

val postModule = module {

    factory {
        BookmarkedPostsViewModel(
            userDataRepository = get(),
            fanboxRepository = get(),
        )
    }

    factory {
        PostDetailViewModel(
            fanboxRepository = get(),
            userDataRepository = get(),
            imageDownloader = get(),
        )
    }

    factory {
        PostDetailRootViewModel(
            userDataRepository = get(),
            fanboxRepository = get(),
        )
    }

    factory {
        PostImageViewModel(
            fanboxRepository = get(),
        )
    }

    factory {
        PostSearchViewModel(
            userDataRepository = get(),
            fanboxRepository = get(),
        )
    }
}
