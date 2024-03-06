package me.matsumo.fanbox.core.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.fanbox.FanboxCursor
import me.matsumo.fanbox.core.model.fanbox.FanboxPost
import me.matsumo.fanbox.core.model.fanbox.id.CreatorId
import me.matsumo.fanbox.core.repository.FanboxRepository

class CreatorPostsPagingSource(
    private val creatorId: CreatorId,
    private val fanboxRepository: FanboxRepository,
) : PagingSource<FanboxCursor, FanboxPost>() {

    override suspend fun load(params: LoadParams<FanboxCursor>): LoadResult<FanboxCursor, FanboxPost> {
        return suspendRunCatching {
            if (fanboxRepository.blockedCreators.first().contains(creatorId)) {
                error("Blocked creator: $creatorId")
            }

            fanboxRepository.getCreatorPosts(creatorId, params.key, params.loadSize)
        }.fold(
            onSuccess = {
                LoadResult.Page(
                    data = it.contents,
                    nextKey = it.cursor,
                    prevKey = null,
                )
            },
            onFailure = {
                LoadResult.Error(it)
            },
        )
    }

    override fun getRefreshKey(state: PagingState<FanboxCursor, FanboxPost>): FanboxCursor? = null
}
