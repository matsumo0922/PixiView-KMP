package me.matsumo.fanbox.feature.post.search.common.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail

class PostSearchCreatorPagingSource(
    private val fanboxRepository: FanboxRepository,
    private val query: String,
) : PagingSource<Int, FanboxCreatorDetail>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FanboxCreatorDetail> {
        return suspendRunCatching {
            fanboxRepository.getCreatorFromQuery(query, params.key ?: 0)
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

    override fun getRefreshKey(state: PagingState<Int, FanboxCreatorDetail>): Int? = null
}
