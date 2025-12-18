package me.matsumo.fanbox.core.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.repository.FanboxRepository
import me.matsumo.fankt.fanbox.domain.FanboxCursor
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import kotlin.coroutines.cancellation.CancellationException

class SupportedPostsPagingSource(
    private val fanboxRepository: FanboxRepository,
    private val isHideRestricted: Boolean,
) : PagingSource<FanboxCursor, FanboxPost>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<FanboxCursor>): LoadResult<FanboxCursor, FanboxPost> {
        return try {
            val blockedCreators = fanboxRepository.blockedCreators.first()
            var cursor = params.key
            val visitedCursor = mutableSetOf<FanboxCursor?>()
            var loadResult: LoadResult<FanboxCursor, FanboxPost>? = null

            while (loadResult == null) {
                if (!visitedCursor.add(cursor)) {
                    loadResult = LoadResult.Page<FanboxCursor, FanboxPost>(
                        data = emptyList(),
                        nextKey = null,
                        prevKey = null,
                    )
                    break
                }

                val page = fanboxRepository.getSupportedPosts(cursor, params.loadSize)
                val contents = page.contents
                    .filterNot { isHideRestricted && it.isRestricted }
                    .filterNot { it.user?.creatorId in blockedCreators }

                loadResult = when {
                    contents.isNotEmpty() -> LoadResult.Page(
                        data = contents,
                        nextKey = page.cursor,
                        prevKey = null,
                    )

                    page.cursor == null -> LoadResult.Page(
                        data = emptyList(),
                        nextKey = null,
                        prevKey = null,
                    )

                    else -> null
                }

                if (loadResult == null) {
                    cursor = page.cursor
                }
            }

            loadResult ?: LoadResult.Page<FanboxCursor, FanboxPost>(
                data = emptyList(),
                nextKey = null,
                prevKey = null,
            )
        } catch (exception: Throwable) {
            if (exception is CancellationException) throw exception
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<FanboxCursor, FanboxPost>): FanboxCursor? = null
}
