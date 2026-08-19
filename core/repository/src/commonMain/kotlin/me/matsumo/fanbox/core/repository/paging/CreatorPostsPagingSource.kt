package me.matsumo.fanbox.core.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Cursor
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.repository.FanboxRepository

class CreatorPostsPagingSource(
    private val creatorId: CreatorId,
    private val cursors: List<Cursor>,
    private val fanboxRepository: FanboxRepository,
) : PagingSource<Int, Post>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, Post> {
        val currentIndex = params.key ?: 0
        val nextIndex = currentIndex + 1

        return suspendRunCatching {
            if (fanboxRepository.blockedCreators.first().contains(creatorId)) {
                error("Blocked creator: ${creatorId.value}")
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

    override fun getRefreshKey(state: PagingState<Int, Post>): Int? = null
}
