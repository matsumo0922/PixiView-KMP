package me.matsumo.fanbox.core.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.model.fanbox.Cursor
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.repository.FanboxRepository
import kotlin.coroutines.cancellation.CancellationException

class SupportedPostsPagingSource(
    private val fanboxRepository: FanboxRepository,
    private val isHideRestricted: Boolean,
) : PagingSource<Cursor, Post>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Cursor>): LoadResult<Cursor, Post> {
        return try {
            val blockedCreators = fanboxRepository.blockedCreators.first()
            var cursor = params.key
            val visitedCursor = mutableSetOf<Cursor?>()
            var loadResult: LoadResult<Cursor, Post>? = null

            while (loadResult == null) {
                if (!visitedCursor.add(cursor)) {
                    loadResult = LoadResult.Page<Cursor, Post>(
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

            loadResult ?: LoadResult.Page<Cursor, Post>(
                data = emptyList(),
                nextKey = null,
                prevKey = null,
            )
        } catch (exception: CancellationException) {
            throw exception
        } catch (exception: Throwable) {
            LoadResult.Error(exception)
        }
    }

    override fun getRefreshKey(state: PagingState<Cursor, Post>): Cursor? = null
}
