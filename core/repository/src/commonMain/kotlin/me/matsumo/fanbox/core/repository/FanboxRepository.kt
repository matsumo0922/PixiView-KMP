package me.matsumo.fanbox.core.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.multiplatform.webview.cookie.WebViewCookieManager
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.datastore.BlockDataStore
import me.matsumo.fanbox.core.datastore.BookmarkDataStore
import me.matsumo.fanbox.core.datastore.OldCookieDataStore
import me.matsumo.fanbox.core.datastore.SettingDataStore
import me.matsumo.fanbox.core.datastore.cookie.MigratingFanboxCookieStorage
import me.matsumo.fanbox.core.repository.paging.CreatorPostsPagingSource
import me.matsumo.fanbox.core.repository.paging.HomePostsPagingSource
import me.matsumo.fanbox.core.repository.paging.SearchCreatorsPagingSource
import me.matsumo.fanbox.core.repository.paging.SearchPostsPagingSource
import me.matsumo.fanbox.core.repository.paging.SupportedPostsPagingSource
import me.matsumo.fankt.fanbox.Fanbox
import me.matsumo.fankt.fanbox.FanboxCookieRecord
import me.matsumo.fankt.fanbox.FanboxLogLevel
import me.matsumo.fankt.fanbox.domain.FanboxCursor
import me.matsumo.fankt.fanbox.domain.PageCursorInfo
import me.matsumo.fankt.fanbox.domain.PageNumberInfo
import me.matsumo.fankt.fanbox.domain.PageOffsetInfo
import me.matsumo.fankt.fanbox.domain.model.FanboxBell
import me.matsumo.fankt.fanbox.domain.model.FanboxComment
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlan
import me.matsumo.fankt.fanbox.domain.model.FanboxCreatorPlanDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxMetaData
import me.matsumo.fankt.fanbox.domain.model.FanboxNewsLetter
import me.matsumo.fankt.fanbox.domain.model.FanboxPaidRecord
import me.matsumo.fankt.fanbox.domain.model.FanboxPost
import me.matsumo.fankt.fanbox.domain.model.FanboxPostDetail
import me.matsumo.fankt.fanbox.domain.model.FanboxTag
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCommentId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxCreatorId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxPostId
import me.matsumo.fankt.fanbox.domain.model.id.FanboxUserId
import org.koin.core.component.KoinComponent
import kotlin.random.Random

interface FanboxRepository {
    val bookmarkedPostsIds: SharedFlow<List<FanboxPostId>>
    val blockedCreators: SharedFlow<Set<FanboxCreatorId>>
    val sessionId: Flow<String?>
    val csrfToken: Flow<String?>
    val logoutTrigger: Flow<Long>

    suspend fun logout()
    suspend fun setSessionId(sessionId: String)

    /**
     * 現在保存されているセッションを返す。無ければ null を返す。
     *
     * [sessionId] は購読を開始した時点の値を流すため、書き込んだ結果を確かめる用途には使えない。
     * 保存先へ書いた内容を読み直す場合はこちらを使う。
     */
    suspend fun getStoredSessionId(): String?

    /**
     * Room 導入より前の保存先に残っているセッションを取り込む。取り込めた場合は true を返す。
     *
     * 読み出しから取り込み元の削除までをログアウトと同じ順序制御の下で行うため、
     * 取り込みの途中でログアウトが挟まってもセッションは復活しない。
     */
    suspend fun importPreRoomSession(): Boolean

    suspend fun setCookies(cookies: List<FanboxCookieRecord>)
    suspend fun updateCsrfToken()
    suspend fun getMetadata(): FanboxMetaData

    suspend fun getHomePosts(
        cursor: FanboxCursor?,
        loadSize: Int = cursor?.limit ?: 10,
    ): PageCursorInfo<FanboxPost>

    suspend fun getSupportedPosts(
        cursor: FanboxCursor?,
        loadSize: Int = cursor?.limit ?: 10,
    ): PageCursorInfo<FanboxPost>

    suspend fun getCreatorPosts(
        creatorId: FanboxCreatorId,
        currentCursor: FanboxCursor,
        nextCursor: FanboxCursor?,
        loadSize: Int = currentCursor.limit ?: 10,
    ): PageCursorInfo<FanboxPost>

    suspend fun getCreatorPostsPagination(creatorId: FanboxCreatorId): List<FanboxCursor>
    suspend fun getPostDetail(postId: FanboxPostId): FanboxPostDetail
    suspend fun getPostDetailCached(postId: FanboxPostId): FanboxPostDetail
    suspend fun getPostComment(
        postId: FanboxPostId,
        offset: Int = 0,
    ): PageOffsetInfo<FanboxComment>

    suspend fun getPostFromQuery(
        query: String,
        creatorId: FanboxCreatorId? = null,
        page: Int = 0,
    ): PageNumberInfo<FanboxPost>

    suspend fun getCreatorFromQuery(
        query: String,
        page: Int = 0,
    ): PageNumberInfo<FanboxCreatorDetail>

    suspend fun getTagFromQuery(query: String): List<FanboxTag>

    suspend fun getHomePostsPager(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<FanboxPost>>

    suspend fun getHomePostsPagerCache(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<FanboxPost>>

    suspend fun getSupportedPostsPager(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<FanboxPost>>

    suspend fun getSupportedPostsPagerCache(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<FanboxPost>>

    suspend fun getCreatorPostsPager(
        creatorId: FanboxCreatorId,
        loadSize: Int,
    ): Flow<PagingData<FanboxPost>>

    suspend fun getCreatorPostsPagerCache(): Flow<PagingData<FanboxPost>>?
    suspend fun getPostsFromQueryPager(
        query: String,
        creatorId: FanboxCreatorId? = null,
    ): Flow<PagingData<FanboxPost>>

    suspend fun getPostsFromQueryPagerCache(): Flow<PagingData<FanboxPost>>?
    suspend fun getCreatorsFromQueryPager(query: String): Flow<PagingData<FanboxCreatorDetail>>

    suspend fun getFollowingCreators(): List<FanboxCreatorDetail>
    suspend fun getFollowingPixivCreators(): List<FanboxCreatorDetail>
    suspend fun getRecommendedCreators(): List<FanboxCreatorDetail>

    suspend fun getCreatorDetail(creatorId: FanboxCreatorId): FanboxCreatorDetail
    suspend fun getCreatorDetailCached(creatorId: FanboxCreatorId): FanboxCreatorDetail
    suspend fun getCreatorTags(creatorId: FanboxCreatorId): List<FanboxTag>

    suspend fun getSupportedPlans(): List<FanboxCreatorPlan>
    suspend fun getCreatorPlans(creatorId: FanboxCreatorId): List<FanboxCreatorPlan>
    suspend fun getCreatorPlan(creatorId: FanboxCreatorId): FanboxCreatorPlanDetail

    suspend fun getPaidRecords(): List<FanboxPaidRecord>
    suspend fun getUnpaidRecords(): List<FanboxPaidRecord>

    suspend fun getNewsLetters(): List<FanboxNewsLetter>
    suspend fun getBells(page: Int = 0): PageNumberInfo<FanboxBell>

    suspend fun likePost(postId: FanboxPostId)
    suspend fun likeComment(commentId: FanboxCommentId)

    suspend fun addComment(
        postId: FanboxPostId,
        comment: String,
        rootCommentId: FanboxCommentId? = null,
        parentCommentId: FanboxCommentId? = null,
    )

    suspend fun deleteComment(commentId: FanboxCommentId)

    suspend fun followCreator(creatorUserId: FanboxUserId)
    suspend fun unfollowCreator(creatorUserId: FanboxUserId)

    suspend fun blockCreator(creatorId: FanboxCreatorId)
    suspend fun unblockCreator(creatorId: FanboxCreatorId)

    suspend fun getBookmarkedPosts(): List<FanboxPost>
    suspend fun bookmarkPost(post: FanboxPost)
    suspend fun unbookmarkPost(post: FanboxPost)

    suspend fun setCreatorAllPostsCache(creatorId: FanboxCreatorId, posts: List<FanboxPost>)
    suspend fun getCreatorAllPostsCache(creatorId: FanboxCreatorId): List<FanboxPost>?

    /**
     * ダウンロードした内容を [onChunk] へ順に渡す。
     *
     * 各チャンクは独立した値で、次の読み出しは [onChunk] の完了を待つ。[onChunk] が投げた例外と
     * 呼び出し側のキャンセルはそのまま伝播し、いずれの場合もレスポンスは解放される。ファイルへ
     * 書き出す場合は一時的な保存先へ書き、この関数が正常に終わってから本来の位置へ移すこと。
     */
    suspend fun download(
        url: String,
        onDownload: (Float) -> Unit,
        onChunk: suspend (ByteArray) -> Unit,
    )
}

class FanboxRepositoryImpl(
    private val bookmarkDataStore: BookmarkDataStore,
    private val blockDataStore: BlockDataStore,
    private val userDataStore: SettingDataStore,
    private val oldCookieDataStore: OldCookieDataStore,
    private val ioDispatcher: CoroutineDispatcher,
    private val cookieStorage: MigratingFanboxCookieStorage,
) : FanboxRepository, KoinComponent {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    private val fanbox = Fanbox(
        logLevel = FanboxLogLevel.INFO,
        ioDispatcher = ioDispatcher,
        cookieStorage = cookieStorage,
    )

    private val creatorCache = mutableMapOf<FanboxCreatorId, FanboxCreatorDetail>()
    private val postCache = mutableMapOf<FanboxPostId, FanboxPostDetail>()
    private val creatorAllPostsCache = mutableMapOf<FanboxCreatorId, List<FanboxPost>>()
    private var homePostsPager: Flow<PagingData<FanboxPost>>? = null
    private var supportedPostsPager: Flow<PagingData<FanboxPost>>? = null
    private var creatorPostsPager: Flow<PagingData<FanboxPost>>? = null
    private var searchPostsPager: Flow<PagingData<FanboxPost>>? = null

    // ログアウトの完了を待つ呼び出し元を止めないため、受け手がいなくても送信が進む容量を持たせる。
    private val _logoutTrigger = Channel<Long>(Channel.CONFLATED)

    override val sessionId: Flow<String?> =
        fanbox.cookies.map { list -> list.find { it.name == FANBOX_SESSION_ID_NAME }?.value }
    override val csrfToken: Flow<String?> = fanbox.csrfToken
    override val logoutTrigger: Flow<Long> = _logoutTrigger.receiveAsFlow()

    override val bookmarkedPostsIds: SharedFlow<List<FanboxPostId>> = bookmarkDataStore.data
    override val blockedCreators: SharedFlow<Set<FanboxCreatorId>> = blockDataStore.data

    /**
     * ログアウトする。
     *
     * 保存先から資格情報を消し終えてから完了する。以前は空の `FANBOXSESSID` を保存していたが、
     * それではセッションが残ったままになる。
     *
     * WebView の Cookie 削除に失敗しても、保存先の削除は行う。WebView 側の後始末より、
     * 資格情報を消し残さないことを優先する。
     *
     * Room 導入より前の保存先も [cookieStorage] が消す。ログアウト中の印が立っている間に
     * 消すため、途中でプロセスが終わっても取り込み元だけが残ることはない。
     *
     * 呼び出し元が取り消されても最後まで行う。途中で止まると資格情報が消し残り、
     * どこかの保存先から復活するため。
     */
    override suspend fun logout() = withContext(ioDispatcher + NonCancellable) {
        runCatching {
            withContext(Dispatchers.Main) { WebViewCookieManager().removeAllCookies() }
        }.onFailure { failure ->
            Napier.w(failure) { "Failed to remove the WebView Cookies during logout." }
        }

        cookieStorage.logout()

        bookmarkDataStore.clear()
        blockDataStore.clear()
        userDataStore.setTestUser(false)
        userDataStore.setFollowTabDefaultHome(false)

        _logoutTrigger.send(Random.nextLong())
    }

    override suspend fun setSessionId(sessionId: String) {
        fanbox.setFanboxSessionId(sessionId)
    }

    override suspend fun getStoredSessionId(): String? {
        return cookieStorage.snapshot().find { it.name == FANBOX_SESSION_ID_NAME }?.value
    }

    override suspend fun importPreRoomSession(): Boolean = withContext(ioDispatcher) {
        cookieStorage.importPreRoomSession {
            oldCookieDataStore.getSessionId()?.let { sessionId ->
                // fankt の `setFanboxSessionId` が保存する形と同じにする。
                FanboxCookieRecord(
                    domain = ".fanbox.cc",
                    path = "/",
                    name = FANBOX_SESSION_ID_NAME,
                    value = sessionId,
                    expiresAtEpochMilliseconds = null,
                    secure = true,
                    hostOnly = false,
                )
            }
        }
    }

    override suspend fun setCookies(cookies: List<FanboxCookieRecord>) {
        fanbox.setCookies(cookies, reset = true)
    }

    override suspend fun updateCsrfToken() {
        fanbox.updateCsrfToken()
    }

    override suspend fun getMetadata(): FanboxMetaData {
        return fanbox.getMetadata()
    }

    override suspend fun getHomePosts(
        cursor: FanboxCursor?,
        loadSize: Int,
    ): PageCursorInfo<FanboxPost> {
        return fanbox.getHomePosts(cursor)
    }

    override suspend fun getSupportedPosts(
        cursor: FanboxCursor?,
        loadSize: Int,
    ): PageCursorInfo<FanboxPost> {
        return fanbox.getSupportedPosts(cursor)
    }

    override suspend fun getCreatorPosts(
        creatorId: FanboxCreatorId,
        currentCursor: FanboxCursor,
        nextCursor: FanboxCursor?,
        loadSize: Int,
    ): PageCursorInfo<FanboxPost> {
        return fanbox.getCreatorPosts(creatorId, currentCursor, nextCursor)
    }

    override suspend fun getPostFromQuery(
        query: String,
        creatorId: FanboxCreatorId?,
        page: Int,
    ): PageNumberInfo<FanboxPost> {
        return fanbox.getPostFromQuery(query, creatorId, page)
    }

    override suspend fun getCreatorPostsPagination(creatorId: FanboxCreatorId): List<FanboxCursor> {
        return fanbox.getCreatorPostsPagination(creatorId)
    }

    override suspend fun getCreatorFromQuery(
        query: String,
        page: Int,
    ): PageNumberInfo<FanboxCreatorDetail> {
        return fanbox.searchCreators(query, page)
    }

    override suspend fun getTagFromQuery(query: String): List<FanboxTag> {
        return fanbox.searchTags(query)
    }

    override suspend fun getPostDetail(postId: FanboxPostId): FanboxPostDetail {
        return fanbox.getPostDetail(postId)
    }

    override suspend fun getPostDetailCached(postId: FanboxPostId): FanboxPostDetail =
        withContext(ioDispatcher) {
            postCache.getOrPut(postId) { getPostDetail(postId) }
        }

    override suspend fun getPostComment(
        postId: FanboxPostId,
        offset: Int,
    ): PageOffsetInfo<FanboxComment> {
        return fanbox.getPostComment(postId, offset)
    }

    override suspend fun getHomePostsPager(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<FanboxPost>> {
        return Pager(
            config = PagingConfig(pageSize = loadSize),
            initialKey = null,
            pagingSourceFactory = {
                HomePostsPagingSource(this, isHideRestricted)
            },
        )
            .flow
            .cachedIn(scope)
            .also { homePostsPager = it }
    }

    override suspend fun getHomePostsPagerCache(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<FanboxPost>> {
        return homePostsPager ?: getHomePostsPager(loadSize, isHideRestricted)
    }

    override suspend fun getSupportedPostsPager(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<FanboxPost>> {
        return Pager(
            config = PagingConfig(pageSize = loadSize),
            initialKey = null,
            pagingSourceFactory = {
                SupportedPostsPagingSource(this, isHideRestricted)
            },
        )
            .flow
            .cachedIn(scope)
            .also { supportedPostsPager = it }
    }

    override suspend fun getSupportedPostsPagerCache(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<FanboxPost>> {
        return supportedPostsPager ?: getSupportedPostsPager(loadSize, isHideRestricted)
    }

    override suspend fun getCreatorPostsPager(
        creatorId: FanboxCreatorId,
        loadSize: Int,
    ): Flow<PagingData<FanboxPost>> {
        val cursors = getCreatorPostsPagination(creatorId)

        return Pager(
            config = PagingConfig(pageSize = loadSize),
            initialKey = null,
            pagingSourceFactory = {
                CreatorPostsPagingSource(
                    creatorId = creatorId,
                    cursors = cursors,
                    fanboxRepository = this,
                )
            },
        )
            .flow
            .cachedIn(scope)
            .also { creatorPostsPager = it }
    }

    override suspend fun getCreatorPostsPagerCache(): Flow<PagingData<FanboxPost>>? {
        return creatorPostsPager
    }

    override suspend fun getPostsFromQueryPager(
        query: String,
        creatorId: FanboxCreatorId?,
    ): Flow<PagingData<FanboxPost>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            initialKey = null,
            pagingSourceFactory = {
                SearchPostsPagingSource(this, creatorId, query)
            },
        )
            .flow
            .cachedIn(scope)
            .also { searchPostsPager = it }
    }

    override suspend fun getPostsFromQueryPagerCache(): Flow<PagingData<FanboxPost>>? {
        return searchPostsPager
    }

    override suspend fun getCreatorsFromQueryPager(query: String): Flow<PagingData<FanboxCreatorDetail>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            initialKey = null,
            pagingSourceFactory = {
                SearchCreatorsPagingSource(this, query)
            },
        ).flow
    }

    override suspend fun getFollowingCreators(): List<FanboxCreatorDetail> {
        return fanbox.getFollowingCreators()
    }

    override suspend fun getFollowingPixivCreators(): List<FanboxCreatorDetail> {
        return fanbox.getFollowingPixivCreators()
    }

    override suspend fun getRecommendedCreators(): List<FanboxCreatorDetail> {
        return fanbox.getRecommendedCreators()
    }

    override suspend fun getCreatorDetail(creatorId: FanboxCreatorId): FanboxCreatorDetail {
        return fanbox.getCreatorDetail(creatorId)
    }

    override suspend fun getCreatorDetailCached(creatorId: FanboxCreatorId): FanboxCreatorDetail =
        withContext(ioDispatcher) {
            creatorCache.getOrPut(creatorId) { getCreatorDetail(creatorId) }
        }

    override suspend fun getCreatorTags(creatorId: FanboxCreatorId): List<FanboxTag> {
        return fanbox.getCreatorTags(creatorId)
    }

    override suspend fun getSupportedPlans(): List<FanboxCreatorPlan> {
        return fanbox.getSupportedPlans()
    }

    override suspend fun getCreatorPlans(creatorId: FanboxCreatorId): List<FanboxCreatorPlan> {
        return fanbox.getCreatorPlans(creatorId)
    }

    override suspend fun getCreatorPlan(creatorId: FanboxCreatorId): FanboxCreatorPlanDetail {
        return fanbox.getCreatorPlanDetail(creatorId)
    }

    override suspend fun getPaidRecords(): List<FanboxPaidRecord> {
        return fanbox.getPaidRecords()
    }

    override suspend fun getUnpaidRecords(): List<FanboxPaidRecord> {
        return fanbox.getUnpaidRecords()
    }

    override suspend fun getNewsLetters(): List<FanboxNewsLetter> {
        return fanbox.getNewsLetters()
    }

    override suspend fun getBells(page: Int): PageNumberInfo<FanboxBell> {
        // 一覧を取得した時点で FANBOX 側の通知を既読にする。fankt 0.1.0 の既定は未読のまま残す
        // 挙動だが、PixiView は従来どおり既読化する。
        return fanbox.getBells(page, markNotificationsRead = true)
    }

    override suspend fun likePost(postId: FanboxPostId) {
        fanbox.likePost(postId)
    }

    override suspend fun likeComment(commentId: FanboxCommentId) {
        fanbox.likeComment(commentId)
    }

    override suspend fun addComment(
        postId: FanboxPostId,
        comment: String,
        rootCommentId: FanboxCommentId?,
        parentCommentId: FanboxCommentId?,
    ) {
        fanbox.addComment(
            postId = postId,
            rootCommentId = rootCommentId,
            parentCommentId = parentCommentId,
            body = comment,
        )
    }

    override suspend fun deleteComment(commentId: FanboxCommentId) {
        fanbox.deleteComment(commentId)
    }

    override suspend fun followCreator(creatorUserId: FanboxUserId) {
        fanbox.followCreator(creatorUserId)
    }

    override suspend fun unfollowCreator(creatorUserId: FanboxUserId) {
        fanbox.unfollowCreator(creatorUserId)
    }

    override suspend fun blockCreator(creatorId: FanboxCreatorId) {
        blockDataStore.blockCreator(creatorId)
    }

    override suspend fun unblockCreator(creatorId: FanboxCreatorId) {
        blockDataStore.unblockCreator(creatorId)
    }

    override suspend fun getBookmarkedPosts(): List<FanboxPost> = withContext(ioDispatcher) {
        bookmarkDataStore.get()
    }

    override suspend fun bookmarkPost(post: FanboxPost) = withContext(ioDispatcher) {
        bookmarkDataStore.save(post)
    }

    override suspend fun unbookmarkPost(post: FanboxPost) {
        bookmarkDataStore.remove(post)
    }

    override suspend fun setCreatorAllPostsCache(creatorId: FanboxCreatorId, posts: List<FanboxPost>) {
        creatorAllPostsCache[creatorId] = posts
    }

    override suspend fun getCreatorAllPostsCache(creatorId: FanboxCreatorId): List<FanboxPost>? {
        return creatorAllPostsCache[creatorId]
    }

    override suspend fun download(
        url: String,
        onDownload: (Float) -> Unit,
        onChunk: suspend (ByteArray) -> Unit,
    ) {
        fanbox.download(
            url = url,
            onProgress = onDownload,
            onChunk = onChunk,
        )
    }
}

/** FANBOX のセッション Cookie の名前。 */
private const val FANBOX_SESSION_ID_NAME = "FANBOXSESSID"
