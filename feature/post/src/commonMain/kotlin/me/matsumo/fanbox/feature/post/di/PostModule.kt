package me.matsumo.fanbox.feature.post.di

import me.matsumo.fanbox.feature.post.bookmark.BookmarkedPostsViewModel
import me.matsumo.fanbox.feature.post.detail.PostDetailRootViewModel
import me.matsumo.fanbox.feature.post.detail.PostDetailViewModel
import me.matsumo.fanbox.feature.post.image.PostImageViewModel
import me.matsumo.fanbox.feature.post.queue.DownloadQueueViewModel
import me.matsumo.fanbox.feature.post.search.PostSearchViewModel
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

val postModule = module {
    viewModelOf(::BookmarkedPostsViewModel)
    viewModelOf(::PostDetailViewModel)
    viewModelOf(::PostDetailRootViewModel)
    viewModelOf(::PostImageViewModel)
    viewModelOf(::PostSearchViewModel)
    viewModelOf(::DownloadQueueViewModel)
}
