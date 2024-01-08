package me.matsumo.fanbox.core.ui.extensition

import androidx.paging.LoadState
import androidx.paging.LoadStates
import androidx.paging.PagingData
import app.cash.paging.compose.LazyPagingItems
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

fun <T : Any> emptyPaging(): Flow<PagingData<T>> = flowOf(
    PagingData.empty(
        LoadStates(
            prepend = LoadState.NotLoading(true),
            append = LoadState.NotLoading(true),
            refresh = LoadState.NotLoading(true),
        ),
    ),
)

fun <T : Any> LazyPagingItems<T>.isEmpty(): Boolean {
    val isNotLoading = loadState.refresh is LoadState.NotLoading
    val isReachedEnd = loadState.append.endOfPaginationReached
    val isEmptyItems = itemCount == 0

    return isNotLoading && isReachedEnd && isEmptyItems
}

fun <T : Any> LazyPagingItems<T>?.isNullOrEmpty(): Boolean {
    this ?: return true

    val isNotLoading = loadState.refresh is LoadState.NotLoading
    val isReachedEnd = loadState.append.endOfPaginationReached
    val isEmptyItems = itemCount == 0

    return isNotLoading && isReachedEnd && isEmptyItems
}
