package me.matsumo.fanbox.core.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.common.util.suspendRunCatching
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fankt.fanbox.domain.FanboxCursor
import me.matsumo.fankt.fanbox.domain.model.FanboxPost

class SupportedPostsPagingSource(
    private val fanboxRepository: FanboxRepository,
    private val isHideRestricted: Boolean,
) : PagingSource<FanboxCursor, FanboxPost>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<FanboxCursor>): LoadResult<FanboxCursor, FanboxPost> {
        return suspendRunCatching {
            fanboxRepository.getSupportedPosts(params.key, params.loadSize)
        }.fold(
            onSuccess = { page ->
                val contents = page.contents.toMutableList()

                if (isHideRestricted) {
                    contents.removeAll { it.isRestricted }
                }

                for (blockedCreator in fanboxRepository.blockedCreators.first()) {
                    contents.removeAll { it.user?.creatorId == blockedCreator }
                }

                LoadResult.Page(
                    data = contents,
                    nextKey = page.cursor,
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
