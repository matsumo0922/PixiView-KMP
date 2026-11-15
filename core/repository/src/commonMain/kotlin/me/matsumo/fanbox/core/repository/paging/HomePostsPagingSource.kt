package me.matsumo.fanbox.core.repository.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import kotlinx.coroutines.flow.first
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.Cursor
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.repository.FanboxRepository
import kotlin.coroutines.cancellation.CancellationException

class HomePostsPagingSource(private val fanboxRepository: FanboxRepository, private val isHideRestricted: Boolean) : PagingSource<Cursor, Post>() {

    override val keyReuseSupported: Boolean = true

    override suspend fun load(params: LoadParams<Cursor>): LoadResult<Cursor, Post> = try {
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

            val page = fanboxRepository.getHomePosts(cursor, params.loadSize)
            val contents = filterHomePosts(
                posts = page.contents,
                isHideRestricted = isHideRestricted,
                blockedCreators = blockedCreators,
            )

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

    override fun getRefreshKey(state: PagingState<Cursor, Post>): Cursor? = null
}

/** ホームの投稿一覧に表示する投稿だけを残す。閲覧できない投稿を隠す設定のときはその投稿と、ブロックしたクリエイターの投稿を除く。 */
fun filterHomePosts(
    posts: List<Post>,
    isHideRestricted: Boolean,
    blockedCreators: Set<CreatorId>,
): List<Post> = posts
    .filterNot { isHideRestricted && it.isRestricted }
    .filterNot { it.user?.creatorId in blockedCreators }
