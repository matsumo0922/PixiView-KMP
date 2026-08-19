package me.matsumo.fanbox.feature.library.notify.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.fanbox.Bell
import me.matsumo.fanbox.core.repository.FanboxRepository

class LibraryNotifyPagingSource(
    private val fanboxRepository: FanboxRepository,
) : PagingSource<Int, Bell>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Bell> {
        return suspendRunCatching {
            fanboxRepository.getBells(params.key ?: 1)
        }.fold(
            onSuccess = {
                LoadResult.Page(
                    data = it.contents,
                    nextKey = it.nextPage,
                    prevKey = null,
                )
            },
            onFailure = {
                LoadResult.Error(it)
            },
        )
    }

    override fun getRefreshKey(state: PagingState<Int, Bell>): Int? = null
}
