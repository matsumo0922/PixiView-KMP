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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.withContext
import me.matsumo.fanbox.core.common.PixiViewConfig
import me.matsumo.fanbox.core.common.util.recordException
import me.matsumo.fanbox.core.datastore.BlockDataStore
import me.matsumo.fanbox.core.datastore.BookmarkDataStore
import me.matsumo.fanbox.core.datastore.OldCookieDataStore
import me.matsumo.fanbox.core.datastore.SettingDataStore
import me.matsumo.fanbox.core.datastore.cookie.MigratingFanboxCookieStorage
import me.matsumo.fanbox.core.model.fanbox.Bell
import me.matsumo.fanbox.core.model.fanbox.Comment
import me.matsumo.fanbox.core.model.fanbox.CommentId
import me.matsumo.fanbox.core.model.fanbox.CreatorDetail
import me.matsumo.fanbox.core.model.fanbox.CreatorId
import me.matsumo.fanbox.core.model.fanbox.CreatorPlan
import me.matsumo.fanbox.core.model.fanbox.CreatorPlanDetail
import me.matsumo.fanbox.core.model.fanbox.Cursor
import me.matsumo.fanbox.core.model.fanbox.MetaData
import me.matsumo.fanbox.core.model.fanbox.NewsLetter
import me.matsumo.fanbox.core.model.fanbox.PageCursorInfo
import me.matsumo.fanbox.core.model.fanbox.PageNumberInfo
import me.matsumo.fanbox.core.model.fanbox.PageOffsetInfo
import me.matsumo.fanbox.core.model.fanbox.PaidRecord
import me.matsumo.fanbox.core.model.fanbox.Post
import me.matsumo.fanbox.core.model.fanbox.PostDetail
import me.matsumo.fanbox.core.model.fanbox.PostId
import me.matsumo.fanbox.core.model.fanbox.Tag
import me.matsumo.fanbox.core.model.fanbox.UserId
import me.matsumo.fanbox.core.repository.mapper.toBell
import me.matsumo.fanbox.core.repository.mapper.toComment
import me.matsumo.fanbox.core.repository.mapper.toCommentId
import me.matsumo.fanbox.core.repository.mapper.toCreatorDetail
import me.matsumo.fanbox.core.repository.mapper.toCreatorId
import me.matsumo.fanbox.core.repository.mapper.toCreatorPlan
import me.matsumo.fanbox.core.repository.mapper.toCreatorPlanDetail
import me.matsumo.fanbox.core.repository.mapper.toCursor
import me.matsumo.fanbox.core.repository.mapper.toFanboxCommentId
import me.matsumo.fanbox.core.repository.mapper.toFanboxCreatorId
import me.matsumo.fanbox.core.repository.mapper.toFanboxCursor
import me.matsumo.fanbox.core.repository.mapper.toFanboxPost
import me.matsumo.fanbox.core.repository.mapper.toFanboxPostId
import me.matsumo.fanbox.core.repository.mapper.toFanboxUserId
import me.matsumo.fanbox.core.repository.mapper.toMetaData
import me.matsumo.fanbox.core.repository.mapper.toNewsLetter
import me.matsumo.fanbox.core.repository.mapper.toPageCursorInfo
import me.matsumo.fanbox.core.repository.mapper.toPageNumberInfo
import me.matsumo.fanbox.core.repository.mapper.toPageOffsetInfo
import me.matsumo.fanbox.core.repository.mapper.toPaidRecord
import me.matsumo.fanbox.core.repository.mapper.toPost
import me.matsumo.fanbox.core.repository.mapper.toPostDetail
import me.matsumo.fanbox.core.repository.mapper.toPostId
import me.matsumo.fanbox.core.repository.mapper.toTag
import me.matsumo.fanbox.core.repository.mapper.toUserId
import me.matsumo.fanbox.core.repository.paging.CreatorPostsPagingSource
import me.matsumo.fanbox.core.repository.paging.HomePostsPagingSource
import me.matsumo.fanbox.core.repository.paging.SearchCreatorsPagingSource
import me.matsumo.fanbox.core.repository.paging.SearchPostsPagingSource
import me.matsumo.fanbox.core.repository.paging.SupportedPostsPagingSource
import me.matsumo.fankt.fanbox.Fanbox
import me.matsumo.fankt.fanbox.FanboxCookieRecord
import me.matsumo.fankt.fanbox.FanboxCookieStorage
import me.matsumo.fankt.fanbox.FanboxException
import me.matsumo.fankt.fanbox.FanboxListItemSchemaMismatch
import me.matsumo.fankt.fanbox.FanboxLogLevel
import org.koin.core.component.KoinComponent
import kotlin.random.Random

interface FanboxRepository {
    val bookmarkedPostsIds: SharedFlow<List<PostId>>
    val blockedCreators: SharedFlow<Set<CreatorId>>
    val sessionId: Flow<String?>
    val csrfToken: Flow<String?>
    val logoutTrigger: Flow<Long>
    val sessionInvalidatedTrigger: Flow<Long>

    suspend fun logout()
    suspend fun setSessionId(sessionId: String)

    /**
     * Room 導入より前の保存先に残っているセッションを取り込む。取り込めた場合は true を返す。
     *
     * 読み出しから取り込み元の削除までをログアウトと同じ順序制御の下で行うため、
     * 取り込みの途中でログアウトが挟まってもセッションは復活しない。
     */
    suspend fun importPreRoomSession(): Boolean

    suspend fun setCookies(cookies: List<FanboxCookieRecord>)
    suspend fun updateCsrfToken()
    suspend fun getMetadata(): MetaData

    suspend fun getHomePosts(
        cursor: Cursor?,
        loadSize: Int = cursor?.limit ?: 10,
    ): PageCursorInfo<Post>

    suspend fun getSupportedPosts(
        cursor: Cursor?,
        loadSize: Int = cursor?.limit ?: 10,
    ): PageCursorInfo<Post>

    suspend fun getCreatorPosts(
        creatorId: CreatorId,
        currentCursor: Cursor,
        nextCursor: Cursor?,
        loadSize: Int = currentCursor.limit ?: 10,
    ): PageCursorInfo<Post>

    suspend fun getCreatorPostsPagination(creatorId: CreatorId): List<Cursor>
    suspend fun getPostDetail(postId: PostId): PostDetail
    suspend fun getPostDetailCached(postId: PostId): PostDetail
    suspend fun getPostComment(
        postId: PostId,
        offset: Int = 0,
    ): PageOffsetInfo<Comment>

    suspend fun getPostFromQuery(
        query: String,
        creatorId: CreatorId? = null,
        page: Int = 0,
    ): PageNumberInfo<Post>

    suspend fun getCreatorFromQuery(
        query: String,
        page: Int = 0,
    ): PageNumberInfo<CreatorDetail>

    suspend fun getTagFromQuery(query: String): List<Tag>

    suspend fun getHomePostsPager(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<Post>>

    suspend fun getHomePostsPagerCache(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<Post>>

    suspend fun getSupportedPostsPager(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<Post>>

    suspend fun getSupportedPostsPagerCache(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<Post>>

    suspend fun getCreatorPostsPager(
        creatorId: CreatorId,
        loadSize: Int,
    ): Flow<PagingData<Post>>

    suspend fun getCreatorPostsPagerCache(): Flow<PagingData<Post>>?
    suspend fun getPostsFromQueryPager(
        query: String,
        creatorId: CreatorId? = null,
    ): Flow<PagingData<Post>>

    suspend fun getPostsFromQueryPagerCache(): Flow<PagingData<Post>>?
    suspend fun getCreatorsFromQueryPager(query: String): Flow<PagingData<CreatorDetail>>

    suspend fun getFollowingCreators(): List<CreatorDetail>
    suspend fun getFollowingPixivCreators(): List<CreatorDetail>
    suspend fun getRecommendedCreators(): List<CreatorDetail>

    suspend fun getCreatorDetail(creatorId: CreatorId): CreatorDetail
    suspend fun getCreatorDetailCached(creatorId: CreatorId): CreatorDetail
    suspend fun getCreatorTags(creatorId: CreatorId): List<Tag>

    suspend fun getSupportedPlans(): List<CreatorPlan>
    suspend fun getCreatorPlans(creatorId: CreatorId): List<CreatorPlan>
    suspend fun getCreatorPlan(creatorId: CreatorId): CreatorPlanDetail

    suspend fun getPaidRecords(): List<PaidRecord>
    suspend fun getUnpaidRecords(): List<PaidRecord>

    suspend fun getNewsLetters(): List<NewsLetter>
    suspend fun getBells(page: Int = 0): PageNumberInfo<Bell>

    suspend fun likePost(postId: PostId)
    suspend fun likeComment(commentId: CommentId)

    suspend fun addComment(
        postId: PostId,
        comment: String,
        rootCommentId: CommentId? = null,
        parentCommentId: CommentId? = null,
    )

    suspend fun deleteComment(commentId: CommentId)

    suspend fun followCreator(creatorUserId: UserId)
    suspend fun unfollowCreator(creatorUserId: UserId)

    suspend fun blockCreator(creatorId: CreatorId)
    suspend fun unblockCreator(creatorId: CreatorId)

    suspend fun getBookmarkedPosts(): List<Post>
    suspend fun bookmarkPost(post: Post)
    suspend fun unbookmarkPost(post: Post)

    suspend fun setCreatorAllPostsCache(creatorId: CreatorId, posts: List<Post>)
    suspend fun getCreatorAllPostsCache(creatorId: CreatorId): List<Post>?

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

/**
 * [Fanbox] を生成する。
 *
 * 配信済みの guest を使うかどうかはプラットフォームによって異なり、fankt は guest 用と直接経路用で
 * 別のコンストラクタを持つ。引数の有無ではなく呼び分けが要るため、生成そのものをプラットフォーム側へ
 * 委ねる。配信先と鍵を Android の source set に置くことで、iOS のバイナリへも含まれない。
 */
internal expect fun createFanbox(
    logLevel: FanboxLogLevel,
    settingDataStore: SettingDataStore,
    ioDispatcher: CoroutineDispatcher,
    cookieStorage: FanboxCookieStorage,
): Fanbox

class FanboxRepositoryImpl(
    private val bookmarkDataStore: BookmarkDataStore,
    private val blockDataStore: BlockDataStore,
    private val userDataStore: SettingDataStore,
    private val oldCookieDataStore: OldCookieDataStore,
    private val ioDispatcher: CoroutineDispatcher,
    private val cookieStorage: MigratingFanboxCookieStorage,
    private val pixiViewConfig: PixiViewConfig,
) : FanboxRepository, KoinComponent {

    private val scope = CoroutineScope(SupervisorJob() + ioDispatcher)

    // fankt は NONE 以外の logLevel でスキーマ不一致ごとに応答本文の断片を出力する。断片には
    // FANBOX と利用者のデータが残るため、リリースビルドでは NONE にして出力経路ごと閉じる。
    private val fanbox = createFanbox(
        logLevel = if (pixiViewConfig.isDebug) FanboxLogLevel.INFO else FanboxLogLevel.NONE,
        settingDataStore = userDataStore,
        ioDispatcher = ioDispatcher,
        cookieStorage = cookieStorage,
    )

    private val creatorCache = mutableMapOf<CreatorId, CreatorDetail>()
    private val postCache = mutableMapOf<PostId, PostDetail>()
    private val creatorAllPostsCache = mutableMapOf<CreatorId, List<Post>>()
    private var homePostsPager: Flow<PagingData<Post>>? = null
    private var supportedPostsPager: Flow<PagingData<Post>>? = null
    private var creatorPostsPager: Flow<PagingData<Post>>? = null
    private var searchPostsPager: Flow<PagingData<Post>>? = null

    // ログアウトの完了を待つ呼び出し元を止めないため、受け手がいなくても送信が進む容量を持たせる。
    private val _logoutTrigger = Channel<Long>(Channel.CONFLATED)
    private val _sessionInvalidatedTrigger = Channel<Long>(Channel.CONFLATED)

    override val sessionId: Flow<String?> =
        fanbox.cookies.map { list -> list.find { it.name == FANBOX_SESSION_ID_NAME }?.value }
    override val csrfToken: Flow<String?> = fanbox.csrfToken
    override val logoutTrigger: Flow<Long> = _logoutTrigger.receiveAsFlow()
    override val sessionInvalidatedTrigger: Flow<Long> = _sessionInvalidatedTrigger.receiveAsFlow()

    override val bookmarkedPostsIds: SharedFlow<List<PostId>> = bookmarkDataStore.data
        .map { ids -> ids.map { it.toPostId() } }
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

    override val blockedCreators: SharedFlow<Set<CreatorId>> = blockDataStore.data
        .map { ids -> ids.map { it.toCreatorId() }.toSet() }
        .shareIn(scope, SharingStarted.Eagerly, replay = 1)

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

    /**
     * fankt の list 応答でスキップされた項目をクラッシュレポート基盤へ記録する。
     *
     * [FanboxListItemSchemaMismatch] は endpoint と indexPath のみを持つため、
     * 送信内容に FANBOX や利用者のデータは含まれない。
     * ログ出力は fankt が同じ内容を Napier へ出すため行わない。
     */
    private fun recordSchemaMismatch(mismatch: FanboxListItemSchemaMismatch) {
        recordException(
            IllegalStateException(
                "FANBOX list item schema mismatch. endpoint=${mismatch.endpoint}, indexPath=${mismatch.indexPath}",
            ),
        )
    }

    /**
     * FANBOX API の認証切れを通知し、呼び出し元へ例外を再送出する。
     *
     * [logout] とセッション設定系は包まない。ログアウト中の 401 で [logoutTrigger] と二重に通知したり、
     * セッションを設定する処理を認証済み API として扱ったりしないため。
     */
    private suspend fun <T> withSessionCheck(block: suspend () -> T): T {
        return try {
            block()
        } catch (exception: FanboxException.Unauthorized) {
            _sessionInvalidatedTrigger.send(Random.nextLong())
            throw exception
        }
    }

    override suspend fun getMetadata(): MetaData = withSessionCheck {
        fanbox.getMetadata().toMetaData()
    }

    override suspend fun getHomePosts(
        cursor: Cursor?,
        loadSize: Int,
    ): PageCursorInfo<Post> = withSessionCheck {
        fanbox.getHomePosts(cursor?.toFanboxCursor(), ::recordSchemaMismatch).toPageCursorInfo { it.toPost() }
    }

    override suspend fun getSupportedPosts(
        cursor: Cursor?,
        loadSize: Int,
    ): PageCursorInfo<Post> = withSessionCheck {
        fanbox.getSupportedPosts(cursor?.toFanboxCursor(), ::recordSchemaMismatch).toPageCursorInfo { it.toPost() }
    }

    override suspend fun getCreatorPosts(
        creatorId: CreatorId,
        currentCursor: Cursor,
        nextCursor: Cursor?,
        loadSize: Int,
    ): PageCursorInfo<Post> = withSessionCheck {
        fanbox.getCreatorPosts(
            creatorId = creatorId.toFanboxCreatorId(),
            cursor = currentCursor.toFanboxCursor(),
            nextCursor = nextCursor?.toFanboxCursor(),
            onItemSchemaMismatch = ::recordSchemaMismatch,
        ).toPageCursorInfo { it.toPost() }
    }

    override suspend fun getPostFromQuery(
        query: String,
        creatorId: CreatorId?,
        page: Int,
    ): PageNumberInfo<Post> = withSessionCheck {
        fanbox.getPostFromQuery(query, creatorId?.toFanboxCreatorId(), page).toPageNumberInfo { it.toPost() }
    }

    override suspend fun getCreatorPostsPagination(creatorId: CreatorId): List<Cursor> = withSessionCheck {
        fanbox.getCreatorPostsPagination(creatorId.toFanboxCreatorId()).map { it.toCursor() }
    }

    override suspend fun getCreatorFromQuery(
        query: String,
        page: Int,
    ): PageNumberInfo<CreatorDetail> = withSessionCheck {
        fanbox.searchCreators(query, page).toPageNumberInfo { it.toCreatorDetail() }
    }

    override suspend fun getTagFromQuery(query: String): List<Tag> = withSessionCheck {
        fanbox.searchTags(query).map { it.toTag() }
    }

    override suspend fun getPostDetail(postId: PostId): PostDetail = withSessionCheck {
        fanbox.getPostDetail(postId.toFanboxPostId()).toPostDetail()
    }

    override suspend fun getPostDetailCached(postId: PostId): PostDetail =
        withContext(ioDispatcher) {
            postCache.getOrPut(postId) { getPostDetail(postId) }
        }

    override suspend fun getPostComment(
        postId: PostId,
        offset: Int,
    ): PageOffsetInfo<Comment> = withSessionCheck {
        fanbox.getPostComment(
            postId = postId.toFanboxPostId(),
            offset = offset,
            onItemSchemaMismatch = ::recordSchemaMismatch,
        ).toPageOffsetInfo { it.toComment() }
    }

    override suspend fun getHomePostsPager(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<Post>> {
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
    ): Flow<PagingData<Post>> {
        return homePostsPager ?: getHomePostsPager(loadSize, isHideRestricted)
    }

    override suspend fun getSupportedPostsPager(
        loadSize: Int,
        isHideRestricted: Boolean,
    ): Flow<PagingData<Post>> {
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
    ): Flow<PagingData<Post>> {
        return supportedPostsPager ?: getSupportedPostsPager(loadSize, isHideRestricted)
    }

    override suspend fun getCreatorPostsPager(
        creatorId: CreatorId,
        loadSize: Int,
    ): Flow<PagingData<Post>> {
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

    override suspend fun getCreatorPostsPagerCache(): Flow<PagingData<Post>>? {
        return creatorPostsPager
    }

    override suspend fun getPostsFromQueryPager(
        query: String,
        creatorId: CreatorId?,
    ): Flow<PagingData<Post>> {
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

    override suspend fun getPostsFromQueryPagerCache(): Flow<PagingData<Post>>? {
        return searchPostsPager
    }

    override suspend fun getCreatorsFromQueryPager(query: String): Flow<PagingData<CreatorDetail>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            initialKey = null,
            pagingSourceFactory = {
                SearchCreatorsPagingSource(this, query)
            },
        ).flow
    }

    override suspend fun getFollowingCreators(): List<CreatorDetail> = withSessionCheck {
        fanbox.getFollowingCreators(::recordSchemaMismatch).map { it.toCreatorDetail() }
    }

    override suspend fun getFollowingPixivCreators(): List<CreatorDetail> = withSessionCheck {
        fanbox.getFollowingPixivCreators(::recordSchemaMismatch).map { it.toCreatorDetail() }
    }

    override suspend fun getRecommendedCreators(): List<CreatorDetail> = withSessionCheck {
        fanbox.getRecommendedCreators(::recordSchemaMismatch).map { it.toCreatorDetail() }
    }

    override suspend fun getCreatorDetail(creatorId: CreatorId): CreatorDetail = withSessionCheck {
        fanbox.getCreatorDetail(creatorId.toFanboxCreatorId()).toCreatorDetail()
    }

    override suspend fun getCreatorDetailCached(creatorId: CreatorId): CreatorDetail =
        withContext(ioDispatcher) {
            creatorCache.getOrPut(creatorId) { getCreatorDetail(creatorId) }
        }

    override suspend fun getCreatorTags(creatorId: CreatorId): List<Tag> = withSessionCheck {
        fanbox.getCreatorTags(creatorId.toFanboxCreatorId()).map { it.toTag() }
    }

    override suspend fun getSupportedPlans(): List<CreatorPlan> = withSessionCheck {
        fanbox.getSupportedPlans(::recordSchemaMismatch).map { it.toCreatorPlan() }
    }

    override suspend fun getCreatorPlans(creatorId: CreatorId): List<CreatorPlan> = withSessionCheck {
        fanbox.getCreatorPlans(creatorId.toFanboxCreatorId(), ::recordSchemaMismatch).map { it.toCreatorPlan() }
    }

    override suspend fun getCreatorPlan(creatorId: CreatorId): CreatorPlanDetail = withSessionCheck {
        fanbox.getCreatorPlanDetail(creatorId.toFanboxCreatorId()).toCreatorPlanDetail()
    }

    override suspend fun getPaidRecords(): List<PaidRecord> = withSessionCheck {
        fanbox.getPaidRecords().map { it.toPaidRecord() }
    }

    override suspend fun getUnpaidRecords(): List<PaidRecord> = withSessionCheck {
        fanbox.getUnpaidRecords().map { it.toPaidRecord() }
    }

    override suspend fun getNewsLetters(): List<NewsLetter> = withSessionCheck {
        fanbox.getNewsLetters().map { it.toNewsLetter() }
    }

    override suspend fun getBells(page: Int): PageNumberInfo<Bell> = withSessionCheck {
        // 一覧を取得した時点で FANBOX 側の通知を既読にする。fankt 0.1.0 の既定は未読のまま残す
        // 挙動だが、PixiView は従来どおり既読化する。
        fanbox.getBells(
            page = page,
            onItemSchemaMismatch = ::recordSchemaMismatch,
            markNotificationsRead = true,
        ).toPageNumberInfo { it.toBell() }
    }

    override suspend fun likePost(postId: PostId) = withSessionCheck {
        fanbox.likePost(postId.toFanboxPostId())
    }

    override suspend fun likeComment(commentId: CommentId) = withSessionCheck {
        fanbox.likeComment(commentId.toFanboxCommentId())
    }

    override suspend fun addComment(
        postId: PostId,
        comment: String,
        rootCommentId: CommentId?,
        parentCommentId: CommentId?,
    ) = withSessionCheck {
        fanbox.addComment(
            postId = postId.toFanboxPostId(),
            rootCommentId = rootCommentId?.toFanboxCommentId(),
            parentCommentId = parentCommentId?.toFanboxCommentId(),
            body = comment,
        )
    }

    override suspend fun deleteComment(commentId: CommentId) = withSessionCheck {
        fanbox.deleteComment(commentId.toFanboxCommentId())
    }

    override suspend fun followCreator(creatorUserId: UserId) = withSessionCheck {
        fanbox.followCreator(creatorUserId.toFanboxUserId())
    }

    override suspend fun unfollowCreator(creatorUserId: UserId) = withSessionCheck {
        fanbox.unfollowCreator(creatorUserId.toFanboxUserId())
    }

    override suspend fun blockCreator(creatorId: CreatorId) {
        blockDataStore.blockCreator(creatorId.toFanboxCreatorId())
    }

    override suspend fun unblockCreator(creatorId: CreatorId) {
        blockDataStore.unblockCreator(creatorId.toFanboxCreatorId())
    }

    override suspend fun getBookmarkedPosts(): List<Post> = withContext(ioDispatcher) {
        bookmarkDataStore.get().map { it.toPost() }
    }

    override suspend fun bookmarkPost(post: Post) = withContext(ioDispatcher) {
        bookmarkDataStore.save(post.toFanboxPost())
    }

    override suspend fun unbookmarkPost(post: Post) {
        bookmarkDataStore.remove(post.toFanboxPost())
    }

    override suspend fun setCreatorAllPostsCache(creatorId: CreatorId, posts: List<Post>) {
        creatorAllPostsCache[creatorId] = posts
    }

    override suspend fun getCreatorAllPostsCache(creatorId: CreatorId): List<Post>? {
        return creatorAllPostsCache[creatorId]
    }

    override suspend fun download(
        url: String,
        onDownload: (Float) -> Unit,
        onChunk: suspend (ByteArray) -> Unit,
    ) = withSessionCheck {
        fanbox.download(
            url = url,
            onProgress = onDownload,
            onChunk = onChunk,
        )
    }
}

/** FANBOX のセッション Cookie の名前。 */
private const val FANBOX_SESSION_ID_NAME = "FANBOXSESSID"
