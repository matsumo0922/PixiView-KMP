package me.matsumo.fanbox.core.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.repository.FanboxRepository

class SearchPostsPagingSource(
    private val fanboxRepository: FanboxRepository,
    private val creatorId: CreatorId?,
    private val tag: String,
) : PagingSource<Int, FanboxPost>() {

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FanboxPost> {
        return suspendRunCatching {
            fanboxRepository.getPostFromQuery(tag, creatorId, params.key ?: 0)
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

    override fun getRefreshKey(state: PagingState<Int, FanboxPost>): Int? = null
}
