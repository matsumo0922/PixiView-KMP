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
    private val cursors: List<FanboxCursor>,
    private val fanboxRepository: FanboxRepository,
) : PagingSource<Int, FanboxPost>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, FanboxPost> {
        val currentIndex = params.key ?: 0
        val nextIndex = currentIndex + 1

        return suspendRunCatching {
            if (fanboxRepository.blockedCreators.first().contains(creatorId)) {
                error("Blocked creator: $creatorId")
            }

            fanboxRepository.getCreatorPosts(
                creatorId = creatorId,
                currentCursor = cursors[currentIndex],
                nextCursor = cursors.elementAtOrNull(nextIndex),
                loadSize = params.loadSize,
            )
        }.fold(
            onSuccess = {
                LoadResult.Page(
                    data = it.contents,
                    nextKey = if (cursors.size > nextIndex) nextIndex else null,
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
